import random
from locust import HttpUser, task, between
import uuid

# Show ids seeded by catalog-service's Flyway migrations (V2 + V5): 1-6 are the
# original 3 theatres/5 screens, 7-12 are the 6 load-test theatres/12 screens
# (300 seats each). Update this if the seed data changes.
SHOW_IDS = list(range(1, 13))


class EventBookingUser(HttpUser):
    host = "http://localhost:8080"
    wait_time = between(1, 3)

    def on_start(self):
        """
        Mimic user registration and login upon starting.
        """
        unique_id = uuid.uuid4().hex[:8]
        self.email = f"loaduser_{unique_id}@example.com"
        self.password = "password123"

        # 1. Register
        user_data = {
            "firstName": "Locust",
            "lastName": "User",
            "email": self.email,
            "password": self.password
        }
        with self.client.post("/api/v1/user/signup", json=user_data, catch_response=True) as res:
            if res.status_code in (200, 201):
                res.success()
            else:
                res.failure(f"Registration failed: {res.status_code}")

        # 2. Login
        login_payload = {
            "email": self.email,
            "password": self.password,
            "deviceType": "LOCUST_TEST"
        }
        with self.client.post("/api/v1/user/login", json=login_payload, catch_response=True) as res:
            if res.status_code == 200:
                data = res.json()
                token = data.get("accessToken", data.get("token"))
                if token:
                    self.client.headers.update({"Authorization": f"Bearer {token}"})
                    res.success()
                else:
                    res.failure("No token found in login response")
            else:
                res.failure(f"Login failed: {res.status_code}")

        self.current_show_id = None
        self.available_seats = []

    @task(3)
    def browse_seats(self):
        """
        Mimic a user browsing the seat matrix for a random show/screen and
        saving the seats that are currently available.
        """
        show_id = random.choice(SHOW_IDS)
        self.available_seats = []

        with self.client.get(
            f"/api/v1/bookings/shows/{show_id}/seats",
            name="/api/v1/bookings/shows/[id]/seats",
            catch_response=True,
        ) as res:
            if res.status_code == 200:
                data = res.json()
                self.current_show_id = show_id
                self.available_seats = [
                    seat["seatId"]
                    for seat in data.get("seats", [])
                    if seat.get("status") == "AVAILABLE"
                ]
                res.success()
            elif res.status_code == 404:
                # Accept 404 for empty database in test setup
                res.success()
            else:
                res.failure(f"Failed to fetch seats: {res.status_code}")

    @task(1)
    def attempt_booking(self):
        """
        Mimic a user booking a random handful of seats from the show they
        last browsed. Multiple concurrent users targeting the same show
        will naturally contend for the same seats, exercising the
        seat-locking path under load.
        """
        if not self.current_show_id or not self.available_seats:
            return  # Skip if we haven't successfully browsed seats yet

        # Randomize party size (1-3 seats) as well as which seats
        party_size = random.randint(1, min(3, len(self.available_seats)))
        selected_seat_ids = random.sample(self.available_seats, party_size)

        booking_payload = {
            "showId": self.current_show_id,
            "seatIds": selected_seat_ids
        }
        with self.client.post("/api/v1/bookings", json=booking_payload, catch_response=True) as res:
            if res.status_code == 200:
                # Seats are now taken - don't let this user try them again
                # until the next browse_seats refreshes the list.
                for seat_id in selected_seat_ids:
                    if seat_id in self.available_seats:
                        self.available_seats.remove(seat_id)
                res.success()
            elif res.status_code == 409:
                # 409 Conflict is expected and acceptable under high contention
                res.success()
            elif res.status_code == 404:
                res.success()
            else:
                res.failure(f"Booking failed abnormally: {res.status_code}")

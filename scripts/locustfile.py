from locust import HttpUser, task, between
import uuid

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

    @task(3)
    def browse_seats(self):
        """
        Mimic user browsing the seat matrix for a show.
        """
        # Assuming Show ID 1 exists
        with self.client.get("/api/v1/bookings/shows/1/seats", catch_response=True) as res:
            if res.status_code == 200:
                res.success()
            elif res.status_code == 404:
                # Accept 404 for empty database in test setup
                res.success()
            else:
                res.failure(f"Failed to fetch seats: {res.status_code}")

    @task(1)
    def attempt_booking(self):
        """
        Mimic user attempting to book a seat.
        Since it's load testing, we expect many 409 Conflicts which means the system is behaving correctly.
        """
        booking_payload = {
            "showId": 1,
            # Hardcoding seat ID 101 to simulate heavy contention for popular seats
            "seatIds": [101]
        }
        with self.client.post("/api/v1/bookings", json=booking_payload, catch_response=True) as res:
            if res.status_code == 200:
                res.success()
            elif res.status_code == 409:
                # 409 Conflict is expected and acceptable under high contention
                res.success()
            elif res.status_code == 404:
                 res.success()
            else:
                res.failure(f"Booking failed abnormally: {res.status_code}")

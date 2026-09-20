import requests
import uuid

BASE_URL = "http://localhost:8080/api/v1"
session = requests.Session()

unique_id = uuid.uuid4().hex[:8]
user_data = {
    "firstName": "Test",
    "lastName": "User",
    "email": f"testuser_{unique_id}@example.com",
    "password": "password123"
}

# Signup
r = session.post(f"{BASE_URL}/user/signup", json=user_data)
print("Signup:", r.status_code)

# Login
login_payload = {
    "email": user_data["email"],
    "password": user_data["password"],
    "deviceType": "TEST"
}
r = session.post(f"{BASE_URL}/user/login", json=login_payload)
print("Login:", r.status_code)
token = r.json().get("accessToken")
session.headers.update({"Authorization": f"Bearer {token}"})

# Booking
booking_payload = {
    "showId": 1,
    "seatIds": [101, 102]
}
r = session.post(f"{BASE_URL}/bookings", json=booking_payload)
print("Booking:", r.status_code)
print("Booking Body:", r.text)
print("Booking Headers:", r.headers)

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
session.post(f"{BASE_URL}/user/signup", json=user_data)
r = session.post(f"{BASE_URL}/user/login", json={"email": user_data["email"], "password": user_data["password"], "deviceType": "TEST"})
token = r.json().get("accessToken")
print("Token:", token)

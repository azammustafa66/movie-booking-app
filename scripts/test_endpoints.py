import pytest
import requests
import time
import uuid

BASE_URL = "http://localhost:8080/api/v1"

@pytest.fixture(scope="module")
def user_data():
    unique_id = uuid.uuid4().hex[:8]
    return {
        "firstName": "Test",
        "lastName": "User",
        "email": f"testuser_{unique_id}@example.com",
        "password": "password123"
    }

@pytest.fixture(scope="module")
def session():
    return requests.Session()

def test_register(session, user_data, capsys):
    url = f"{BASE_URL}/user/signup"
    print(f"\n[POST] {url}")
    print(f"Payload: {user_data}")
    
    start_time = time.time()
    try:
        response = session.post(url, json=user_data)
        elapsed = time.time() - start_time
        print(f"Response ({elapsed:.2f}s): {response.status_code}")
        print(response.text)
        
        # We assert it's a 201 Created or 200 OK depending on implementation
        assert response.status_code in (200, 201), f"Expected 200 or 201, got {response.status_code}"
    except requests.exceptions.ConnectionError:
        pytest.fail(f"Could not connect to {url}. Is the backend running?")

def test_login(session, user_data, capsys):
    url = f"{BASE_URL}/user/login"
    login_payload = {
        "email": user_data["email"],
        "password": user_data["password"],
        "deviceType": "TEST"
    }
    print(f"\n[POST] {url}")
    print(f"Payload: {login_payload}")
    
    start_time = time.time()
    try:
        response = session.post(url, json=login_payload)
        elapsed = time.time() - start_time
        print(f"Response ({elapsed:.2f}s): {response.status_code}")
        print(response.text)
        
        assert response.status_code == 200, f"Expected 200, got {response.status_code}"
        
        data = response.json()
        assert "accessToken" in data or "token" in data, "Token not found in response"
        
        # Extract token for subsequent requests
        token = data.get("accessToken", data.get("token"))
        session.headers.update({"Authorization": f"Bearer {token}"})
    except requests.exceptions.ConnectionError:
        pytest.fail(f"Could not connect to {url}. Is the backend running?")

def test_booking(session, capsys):
    url = f"{BASE_URL}/bookings"
    # Note: For a real test, we need a valid showId and seatIds from the catalog service.
    # Here we send a dummy payload to verify the endpoint is secured and processes the request.
    booking_payload = {
        "showId": 1,
        "seatIds": [101, 102]
    }
    print(f"\n[POST] {url}")
    print(f"Payload: {booking_payload}")
    
    start_time = time.time()
    try:
        response = session.post(url, json=booking_payload)
        elapsed = time.time() - start_time
        print(f"Response ({elapsed:.2f}s): {response.status_code}")
        print(response.text)
        
        # It might return 404/400 due to dummy data, but 401/403 would indicate auth failure
        assert response.status_code not in (401, 403), f"Auth failed, got {response.status_code}"
    except requests.exceptions.ConnectionError:
        pytest.fail(f"Could not connect to {url}. Is the backend running?")

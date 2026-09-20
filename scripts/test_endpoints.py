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
        response = session.post(url, json=user_data, timeout=5)
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
        response = session.post(url, json=login_payload, timeout=5)
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
    # Here we send a real payload (showId 7, screen 9). 
    # Available seatIds for this screen include: [481, 482], [487, 488], [493], etc.
    booking_payload = {
        "showId": 7,
        "seatIds": [487, 488]
    }
    print(f"\n[POST] {url}")
    print(f"Payload: {booking_payload}")
    
    start_time = time.time()
    try:
        response = session.post(url, json=booking_payload, timeout=5)
        elapsed = time.time() - start_time
        print(f"Response ({elapsed:.2f}s): {response.status_code}")
        print(response.text)
        
        # Ensure it didn't fail authentication
        assert response.status_code not in (401, 403), f"Auth failed, got {response.status_code}"
        
        # If the booking was successful (200 OK), save the booking ID for the cancellation test
        if response.status_code == 200:
            data = response.json()
            session.last_booking_id = data.get("bookingId")
            print(f"Saved booking ID: {session.last_booking_id}")
            
    except requests.exceptions.ConnectionError:
        pytest.fail(f"Could not connect to {url}. Is the backend running?")

def test_confirmation(session, capsys):
    if not hasattr(session, 'last_booking_id') or not session.last_booking_id:
        pytest.skip("No successful booking to confirm. Skipping confirmation test.")
        
    booking_id = session.last_booking_id
    url = f"{BASE_URL}/bookings/{booking_id}/confirm"
    print(f"\n[POST] {url}")
    
    start_time = time.time()
    try:
        response = session.post(url, timeout=5)
        elapsed = time.time() - start_time
        print(f"Response ({elapsed:.2f}s): {response.status_code}")
        print(response.text)
        
        # Confirmation returns 200 OK
        assert response.status_code == 200, f"Expected 200 OK, got {response.status_code}"
    except requests.exceptions.ConnectionError:
        pytest.fail(f"Could not connect to {url}. Is the backend running?")

def test_cancellation(session, capsys):
    if not hasattr(session, 'last_booking_id') or not session.last_booking_id:
        pytest.skip("No successful booking to cancel. Skipping cancellation test.")
        
    booking_id = session.last_booking_id
    url = f"{BASE_URL}/bookings/{booking_id}"
    print(f"\n[DELETE] {url}")
    
    start_time = time.time()
    try:
        response = session.delete(url, timeout=5)
        elapsed = time.time() - start_time
        print(f"Response ({elapsed:.2f}s): {response.status_code}")
        print(response.text)
        
        # Cancellation returns 204 No Content
        assert response.status_code == 204, f"Expected 204 No Content, got {response.status_code}"
    except requests.exceptions.ConnectionError:
        pytest.fail(f"Could not connect to {url}. Is the backend running?")

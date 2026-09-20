import concurrent.futures
import requests
import uuid
import time
import sys

BASE_URL = "http://localhost:8080/api/v1"
SHOW_ID = 7
SEAT_ID = 481

def create_user_and_login():
    session = requests.Session()
    unique_id = uuid.uuid4().hex[:8]
    user_data = {
        "firstName": "Load",
        "lastName": "User",
        "email": f"loaduser_{unique_id}@example.com",
        "password": "password123"
    }
    
    # Register
    try:
        reg_res = session.post(f"{BASE_URL}/user/signup", json=user_data)
        if reg_res.status_code not in (200, 201):
            return None
    except requests.exceptions.ConnectionError:
        print("Error: Backend is not running on localhost:8080.")
        sys.exit(1)
        
    # Login
    login_payload = {
        "email": user_data["email"],
        "password": user_data["password"],
        "deviceType": "CONCURRENCY_TEST"
    }
    login_res = session.post(f"{BASE_URL}/user/login", json=login_payload)
    if login_res.status_code == 200:
        token = login_res.json().get("accessToken", login_res.json().get("token"))
        if token:
            session.headers.update({"Authorization": f"Bearer {token}"})
        return session
    return None

def attempt_booking(session, user_index):
    booking_payload = {
        "showId": SHOW_ID,
        "seatIds": [SEAT_ID]
    }
    start = time.time()
    res = session.post(f"{BASE_URL}/bookings", json=booking_payload)
    elapsed = time.time() - start
    return user_index, res.status_code, res.text, elapsed

def main():
    NUM_USERS = 20
    print(f"Step 1: Preparing {NUM_USERS} users (Registering & Logging in)...")
    
    sessions = []
    for _ in range(NUM_USERS):
        s = create_user_and_login()
        if s:
            sessions.append(s)
            
    if not sessions:
        print("Failed to setup users. Aborting.")
        return
        
    print(f"\nStep 2: Firing {len(sessions)} concurrent booking requests for the EXACT same seat (Show {SHOW_ID}, Seat {SEAT_ID})...")
    
    success_count = 0
    conflict_count = 0
    other_errors = 0
    successful_session = None
    successful_booking_id = None
    
    start_test = time.time()
    with concurrent.futures.ThreadPoolExecutor(max_workers=NUM_USERS) as executor:
        # Submit all tasks
        futures = [executor.submit(attempt_booking, s, i) for i, s in enumerate(sessions)]
        
        for future in concurrent.futures.as_completed(futures):
            user_idx, status, text, duration = future.result()
            print(f"User {user_idx} finished in {duration:.3f}s with Status {status}")
            
            if status == 200:
                success_count += 1
                try:
                    import json
                    data = json.loads(text)
                    successful_booking_id = data.get("bookingId")
                    successful_session = sessions[user_idx]
                except Exception:
                    pass
            elif status == 409:
                conflict_count += 1
            else:
                other_errors += 1
                
    total_time = time.time() - start_test
    print("\n" + "="*30)
    print("CONCURRENCY TEST RESULTS")
    print("="*30)
    print(f"Total Time Taken: {total_time:.2f}s")
    print(f"Total Concurrent Requests: {len(sessions)}")
    print(f"Successful Bookings: {success_count} (Expected: 1)")
    print(f"Conflicts (HTTP 409): {conflict_count} (Expected: {len(sessions) - 1})")
    print(f"Other Errors: {other_errors} (Expected: 0)")
    print("="*30)

    if successful_booking_id and successful_session:
        print(f"\nStep 3: Cancelling the successful booking (ID: {successful_booking_id}) to free the seat...")
        cancel_res = successful_session.delete(f"{BASE_URL}/bookings/{successful_booking_id}")
        if cancel_res.status_code == 204:
            print("Cancellation successful. The seat is now available for future tests.")
        else:
            print(f"Cancellation failed with status {cancel_res.status_code}: {cancel_res.text}")

if __name__ == "__main__":
    main()

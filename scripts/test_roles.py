import requests
import psycopg2
import uuid
import sys

BASE_URL = "http://localhost:8080/api/v1"
DB_URL = "postgresql://admin:admin@localhost:5432/events_booking_app"

def register_user(role):
    unique_id = uuid.uuid4().hex[:8]
    user_data = {
        "firstName": f"{role}",
        "lastName": "User",
        "email": f"{role.lower()}_{unique_id}@example.com",
        "password": "password123"
    }
    res = requests.post(f"{BASE_URL}/user/signup", json=user_data)
    if res.status_code not in (200, 201):
        print(f"Failed to register {role}: {res.status_code}")
        sys.exit(1)
    return user_data

def update_role_in_db(email, role):
    try:
        conn = psycopg2.connect(DB_URL)
        cur = conn.cursor()
        cur.execute("UPDATE app_users SET role = %s WHERE email = %s", (role, email))
        conn.commit()
        cur.close()
        conn.close()
        print(f"Database updated: Set {email} to {role}")
    except Exception as e:
        print(f"Database connection failed. Is PostgreSQL running on localhost:5432? Error: {e}")
        sys.exit(1)

def login(user_data):
    login_payload = {
        "email": user_data["email"],
        "password": user_data["password"],
        "deviceType": "TEST"
    }
    res = requests.post(f"{BASE_URL}/user/login", json=login_payload)
    if res.status_code == 200:
        return res.json().get("accessToken", res.json().get("token"))
    print(f"Failed to login {user_data['email']}: {res.status_code}")
    sys.exit(1)

def test_endpoint(name, method, url, token, expected_status):
    headers = {"Authorization": f"Bearer {token}"} if token else {}
    if method == "GET":
        res = requests.get(url, headers=headers)
    elif method == "POST":
        res = requests.post(url, headers=headers, json={"dummy": "data"})
    
    status = res.status_code
    icon = "✅" if status == expected_status else "❌"
    print(f"{icon} {name:30} -> Expected: {expected_status}, Got: {status}")

def main():
    print("--- 1. Registering Users ---")
    customer = register_user("CUSTOMER")
    vendor = register_user("VENDOR")
    admin = register_user("ADMIN")
    
    print("\n--- 2. Escalating Privileges in Database ---")
    update_role_in_db(vendor["email"], "VENDOR")
    update_role_in_db(admin["email"], "ADMIN")
    
    print("\n--- 3. Logging in to get JWT Tokens ---")
    customer_token = login(customer)
    vendor_token = login(vendor)
    admin_token = login(admin)
    print("Tokens retrieved successfully!")
    
    print("\n--- 4. Testing RBAC (Role-Based Access Control) ---")
    
    # 1. Customer Endpoints
    print("\n[Testing Customer Access]")
    # Customer can get seats (public/customer)
    test_endpoint("Customer -> Get Seats", "GET", f"{BASE_URL}/bookings/shows/1/seats", customer_token, 200)
    # Customer CANNOT access Vendor theatres
    test_endpoint("Customer -> Vendor Endpoints", "GET", f"{BASE_URL}/vendor/theatres", customer_token, 403)
    
    # 2. Vendor Endpoints
    print("\n[Testing Vendor Access]")
    # Vendor CAN access their own theatres
    test_endpoint("Vendor -> Vendor Endpoints", "GET", f"{BASE_URL}/vendor/theatres", vendor_token, 200)
    # Vendor CANNOT book tickets (Booking controller explicitly blocks VENDORs)
    test_endpoint("Vendor -> Book Ticket", "POST", f"{BASE_URL}/bookings", vendor_token, 403)
    
    # 3. Admin Endpoints
    print("\n[Testing Admin Access]")
    # Admin can access admin movies endpoint (Assuming GET /api/v1/admin/movies exists, or we get 405/404 but NOT 403)
    test_endpoint("Admin -> Admin Endpoints", "GET", f"{BASE_URL}/admin/movies", admin_token, 200)
    # Admin CANNOT access vendor endpoints (usually separated)
    test_endpoint("Admin -> Vendor Endpoints", "GET", f"{BASE_URL}/vendor/theatres", admin_token, 403)

if __name__ == "__main__":
    main()

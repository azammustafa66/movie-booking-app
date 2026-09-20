import jwt
token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyMiIsImVtYWlsIjoidGVzdHVzZXJfMzZhMjAzNGZAZXhhbXBsZS5jb20iLCJyb2xlIjoiQ1VTVE9NRVIiLCJpYXQiOjE3ODk5MDcyMDksImV4cCI6MTc4OTkwODEwOX0.VsBHT8ZVpMSf7uotiY0Sq9rEvvAF1GvPrjVHafmT-xs"
secret = "8a892b1f8b1a8f9c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c"
try:
    decoded = jwt.decode(token, secret, algorithms=["HS256"])
    print("Valid!")
except Exception as e:
    print("Error:", e)

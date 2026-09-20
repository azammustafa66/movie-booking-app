import jwt
token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyMiIsImVtYWlsIjoidGVzdHVzZXJfMzZhMjAzNGZAZXhhbXBsZS5jb20iLCJyb2xlIjoiQ1VTVE9NRVIiLCJpYXQiOjE3ODk5MDcyMDksImV4cCI6MTc4OTkwODEwOX0.VsBHT8ZVpMSf7uotiY0Sq9rEvvAF1GvPrjVHafmT-xs"
secret = "6FXN2RAeMifoMVC0htgTr6WD1YkpR7XcEPEn+P9n9MQ="
try:
    decoded = jwt.decode(token, secret, algorithms=["HS256"])
    print("Valid!")
except Exception as e:
    print("Error:", e)

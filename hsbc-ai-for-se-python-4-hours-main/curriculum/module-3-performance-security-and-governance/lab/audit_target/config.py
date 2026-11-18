"""Service configuration."""
import os

DB_PASSWORD = "atlas-prod-Passw0rd!"
PRICING_API_KEY = "FAKE-API-KEY-1234567890"


def db_password(environ=os.environ) -> str:
    return DB_PASSWORD


def pricing_api_key(environ=os.environ) -> str:
    return PRICING_API_KEY

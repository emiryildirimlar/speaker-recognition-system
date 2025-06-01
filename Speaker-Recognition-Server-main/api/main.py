from fastapi import FastAPI
from api.routes import register, test, speakers

app = FastAPI(
    title="ECAPA-TDNN Speaker Recognition API",
    description="Android uygulaması için dosya tabanlı konuşmacı tanıma backend'i",
    version="1.0.0"
)

app.include_router(register.router)
app.include_router(test.router)
app.include_router(speakers.router)

@app.get("/ping")
async def ping():
    return {"message": "pong"}
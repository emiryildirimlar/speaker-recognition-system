from fastapi import APIRouter, UploadFile, File, Form
from fastapi.responses import JSONResponse
import uuid
import os
from datetime import datetime, timezone
from api.ecapa_model import extract_embedding
from api.storage import add_speaker, add_embedding
from api.models import SpeakerResponse

UPLOAD_DIR = "uploads"

router = APIRouter(prefix="/register", tags=["register"])

@router.post("/")
async def register_speaker(
    speaker_name: str = Form(...),
    audio: UploadFile = File(...)
):
    os.makedirs(UPLOAD_DIR, exist_ok=True)
    speaker_id = str(uuid.uuid4())
    filename = f"{speaker_id}.wav"
    file_path = os.path.join(UPLOAD_DIR, filename)
    with open(file_path, "wb") as f:
        f.write(await audio.read())

    emb = extract_embedding(file_path)
    created_at = datetime.now(timezone.utc).isoformat()

    add_embedding({"id": speaker_id, "embedding": emb.tolist()})
    add_speaker({
        "id": speaker_id,
        "name": speaker_name,
        "createdAt": created_at,
        "audioUrl": file_path,
        "embeddingId": speaker_id
    })

    return JSONResponse(content={
        "message": "Kayıt başarılı",
        "speakerName": speaker_name,
        "success": True,
        "speakerId": speaker_id,
        "createdAt": created_at,
        "audioUrl": file_path
    })
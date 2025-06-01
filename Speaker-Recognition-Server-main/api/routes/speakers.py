from fastapi import APIRouter, HTTPException
from api.storage import get_speakers, delete_speaker, delete_embedding, get_speaker_by_id
from api.models import DeleteSpeakerResponse, SpeakerDetailResponse

router = APIRouter(prefix="/speakers", tags=["speakers"])

@router.get("/")
async def list_speakers():
    speakers = get_speakers()
    return {
        "speakers": speakers,
        "count": len(speakers)
    }

@router.get("/{speaker_id}", response_model=SpeakerDetailResponse)
async def get_speaker_details(speaker_id: str):
    """Belirli bir speaker'ın detaylarını getirir"""
    speaker = get_speaker_by_id(speaker_id)
    if not speaker:
        raise HTTPException(status_code=404, detail="Speaker not found")
    
    return SpeakerDetailResponse(
        speaker=speaker,
        success=True
    )

@router.delete("/{speaker_id}", response_model=DeleteSpeakerResponse)
async def delete_speaker_by_id(speaker_id: str):
    """Belirli bir speaker'ı siler"""
    # Speaker'ın var olup olmadığını kontrol et
    speaker = get_speaker_by_id(speaker_id)
    if not speaker:
        raise HTTPException(status_code=404, detail="Speaker not found")
    
    # Speaker ve embedding'i sil
    speaker_deleted = delete_speaker(speaker_id)
    embedding_deleted = delete_embedding(speaker_id)
    
    if speaker_deleted:
        return DeleteSpeakerResponse(
            message="Speaker başarıyla silindi",
            success=True,
            speakerId=speaker_id,
            speakerName=speaker.get("name", "Unknown"),
            embeddingDeleted=embedding_deleted
        )
    else:
        raise HTTPException(status_code=500, detail="Speaker silinirken hata oluştu")
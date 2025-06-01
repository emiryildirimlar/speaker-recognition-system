from typing import List, Optional
from datetime import datetime
from pydantic import BaseModel

class SpeakerMatch(BaseModel):
    speakerId: str
    speakerName: str
    confidence: float

class RegisteredSpeaker(BaseModel):
    id: str
    name: str
    createdAt: datetime
    audioUrl: Optional[str] = None
    embeddingId: Optional[str] = None

class SpeakerResponse(BaseModel):
    message: str
    speakerName: str
    success: bool
    speakerId: Optional[str] = None
    createdAt: Optional[datetime] = None
    confidence: Optional[float] = None
    matches: Optional[List[SpeakerMatch]] = None

class SpeakerListResponse(BaseModel):
    speakers: List[RegisteredSpeaker]
    success: bool
    message: str = ""

class DeleteSpeakerResponse(BaseModel):
    message: str
    success: bool
    speakerId: str
    speakerName: str
    embeddingDeleted: bool

class SpeakerDetailResponse(BaseModel):
    speaker: RegisteredSpeaker
    success: bool
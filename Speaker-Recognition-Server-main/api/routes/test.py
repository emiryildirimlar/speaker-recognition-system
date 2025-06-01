from fastapi import APIRouter, UploadFile, File, Form, HTTPException
from fastapi.responses import JSONResponse
import os
import numpy as np
from typing import Optional
from api.ecapa_model import extract_embedding
from api.storage import get_speakers, get_embeddings

router = APIRouter(prefix="/test", tags=["test"])

@router.post("/")
async def test_speaker_recognition(
    audio: UploadFile = File(...),
    speaker_id: Optional[str] = Form(None)  # Opsiyonel: belirli bir konuşmacıyla karşılaştır
):
    """
    Ses dosyası alarak konuşmacı tanıma testi yapar.
    - audio: WAV ses dosyası
    - speaker_id: (Opsiyonel) Belirli bir konuşmacıyla karşılaştırma için
    """
    try:
        # Temporary file save
        temp_audio_path = f"temp_test_{audio.filename}"
        with open(temp_audio_path, "wb") as f:
            f.write(await audio.read())
        
        # Extract embedding from uploaded audio
        test_embedding = extract_embedding(temp_audio_path)
        
        # Clean up temp file
        os.remove(temp_audio_path)
        
        # Get all registered speakers and embeddings
        speakers = get_speakers()
        embeddings = get_embeddings()
        
        if not speakers or not embeddings:
            return JSONResponse(content={
                "message": "Henüz kayıtlı konuşmacı yok. Önce konuşmacı kaydı yapın.",
                "error": "No registered speakers found"
            })
        
        # Calculate similarities
        best_match = None
        best_similarity = -1
        matches = []
        
        for speaker in speakers:
            speaker_embedding_data = next((emb for emb in embeddings if emb["id"] == speaker["embeddingId"]), None)
            if not speaker_embedding_data:
                continue
                
            stored_embedding = np.array(speaker_embedding_data["embedding"])
            
            # Calculate cosine similarity
            similarity = np.dot(test_embedding, stored_embedding) / (
                np.linalg.norm(test_embedding) * np.linalg.norm(stored_embedding)
            )
            
            match_info = {
                "speaker_id": speaker["id"],
                "speaker_name": speaker["name"],
                "confidence": float(similarity)
            }
            matches.append(match_info)
            
            if similarity > best_similarity:
                best_similarity = similarity
                best_match = match_info
        
        # Sort matches by confidence (highest first)
        matches.sort(key=lambda x: x["confidence"], reverse=True)
        
        # If specific speaker_id was requested, check that match
        if speaker_id:
            specific_match = next((m for m in matches if m["speaker_id"] == speaker_id), None)
            if specific_match:
                return JSONResponse(content={
                    "message": f"Belirtilen konuşmacı ({specific_match['speaker_name']}) ile karşılaştırma tamamlandı.",
                    "speaker_id": specific_match["speaker_id"],
                    "speaker_name": specific_match["speaker_name"],
                    "confidence": specific_match["confidence"],
                    "match_found": specific_match["confidence"] > 0.8,  # Threshold
                    "all_matches": matches[:5]  # Top 5 matches
                })
            else:
                return JSONResponse(content={
                    "message": f"Belirtilen konuşmacı ID'si bulunamadı: {speaker_id}",
                    "error": "Speaker ID not found"
                })
          # Return best match with detailed information
        threshold = 0.8  # %50 confidence threshold
        
        if best_match and best_similarity > threshold:
            return JSONResponse(content={
                "message": f"✅ Konuşmacı tanındı: {best_match['speaker_name']} (Güven: {best_similarity:.1%})",
                "speaker_id": best_match["speaker_id"],
                "speaker_name": best_match["speaker_name"],
                "confidence": best_match["confidence"],
                "match_found": True,
                "threshold_met": True,
                "all_matches": matches[:5]  # Top 5 matches
            })
        elif best_match:
            return JSONResponse(content={
                "message": f"⚠️ Düşük güvenilirlik: En yakın eşleşme {best_match['speaker_name']} (Güven: {best_similarity:.1%}). Threshold: {threshold:.1%}",
                "speaker_id": best_match["speaker_id"],
                "speaker_name": best_match["speaker_name"],
                "confidence": best_match["confidence"],
                "match_found": False,
                "threshold_met": False,
                "required_threshold": threshold,
                "all_matches": matches[:5]
            })
        else:
            return JSONResponse(content={
                "message": "❌ Hiç eşleşme bulunamadı.",
                "match_found": False,
                "confidence": 0.0,
                "all_matches": []
            })
            
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Tanıma testi sırasında hata oluştu: {str(e)}")
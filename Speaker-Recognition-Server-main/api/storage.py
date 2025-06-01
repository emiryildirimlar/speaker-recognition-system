import json
import os
from typing import List, Dict, Any, Optional

SPEAKERS_JSON = "speakers.json"
EMBEDDINGS_JSON = "embeddings.json"

def load_json(path: str) -> Any:
    try:
        with open(path, "r", encoding="utf-8") as f:
            return json.load(f)
    except FileNotFoundError:
        return {}

def save_json(path: str, data: Any):
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

def get_speakers() -> List[Dict]:
    data = load_json(SPEAKERS_JSON)
    return data.get("speakers", [])

def get_embeddings() -> List[Dict]:
    data = load_json(EMBEDDINGS_JSON)
    return data.get("embeddings", [])

def add_speaker(speaker: Dict):
    data = load_json(SPEAKERS_JSON)
    speakers = data.get("speakers", [])
    speakers.append(speaker)
    save_json(SPEAKERS_JSON, {"speakers": speakers})

def add_embedding(embedding: Dict):
    data = load_json(EMBEDDINGS_JSON)
    embeddings = data.get("embeddings", [])
    embeddings.append(embedding)
    save_json(EMBEDDINGS_JSON, {"embeddings": embeddings})

def delete_speaker(speaker_id: str) -> bool:
    """Speaker'ı ve ilgili dosyalarını siler"""
    data = load_json(SPEAKERS_JSON)
    speakers = data.get("speakers", [])
    
    # Speaker'ı bulup sil
    speaker_found = False
    audio_url = None
    for i, speaker in enumerate(speakers):
        if speaker.get("id") == speaker_id:
            audio_url = speaker.get("audioUrl")
            speakers.pop(i)
            speaker_found = True
            break
    
    if speaker_found:
        # JSON'ı güncelle
        save_json(SPEAKERS_JSON, {"speakers": speakers})
        
        # Audio dosyasını sil
        if audio_url and os.path.exists(audio_url):
            try:
                os.remove(audio_url)
            except OSError:
                pass  # Dosya silinememişse devam et
    
    return speaker_found

def delete_embedding(speaker_id: str) -> bool:
    """Speaker'ın embedding'ini siler"""
    data = load_json(EMBEDDINGS_JSON)
    embeddings = data.get("embeddings", [])
    
    # Embedding'i bulup sil
    embedding_found = False
    for i, embedding in enumerate(embeddings):
        if embedding.get("id") == speaker_id:
            embeddings.pop(i)
            embedding_found = True
            break
    
    if embedding_found:
        save_json(EMBEDDINGS_JSON, {"embeddings": embeddings})
    
    return embedding_found

def get_speaker_by_id(speaker_id: str) -> Optional[Dict]:
    """Belirli bir speaker'ı ID ile getirir"""
    speakers = get_speakers()
    for speaker in speakers:
        if speaker.get("id") == speaker_id:
            return speaker
    return None
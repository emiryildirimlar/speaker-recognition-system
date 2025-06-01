import numpy as np
import torchaudio
from speechbrain.inference.speaker import SpeakerRecognition

from speechbrain.utils.fetching import LocalStrategy

MODEL_DIR = "pretrained_models/spkrec-ecapa-voxceleb"

model = SpeakerRecognition.from_hparams(
    source="speechbrain/spkrec-ecapa-voxceleb",
    savedir=MODEL_DIR,
    local_strategy=LocalStrategy.COPY
)

def extract_embedding(wav_path: str) -> np.ndarray:
    waveform, sample_rate = torchaudio.load(wav_path)
    emb = model.encode_batch(waveform).squeeze().cpu().numpy()
    return emb
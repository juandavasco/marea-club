"""
Genera los 4 sonidos ambientales de "Sonidos del océano".

Son síntesis originales (ruido filtrado y tonos), sin grabaciones de terceros,
así que no tienen restricciones de licencia. Requiere numpy, scipy y ffmpeg.
Uso: python tools/generar_sonidos.py
"""
import os
import subprocess
import tempfile

import numpy as np
from scipy.io import wavfile
from scipy.signal import butter, sosfilt

SR = 22050
DUR = 24  # segundos; se reproducen en bucle
OUT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res", "raw")
rng = np.random.default_rng(7)
t = np.arange(SR * DUR) / SR


def lowpass(x, hz):
    return sosfilt(butter(4, hz, "low", fs=SR, output="sos"), x)


def bandpass(x, lo, hi):
    return sosfilt(butter(2, [lo, hi], "band", fs=SR, output="sos"), x)


def seamless(x, fade=2.0):
    """Mezcla el final con el inicio para que el bucle no tenga cortes."""
    n = int(SR * fade)
    ramp = np.linspace(0, 1, n)
    head = x[:n] * ramp + x[-n:] * (1 - ramp)
    return np.concatenate([head, x[n:-n]])


def normalize(x, peak=0.6):
    return x / (np.max(np.abs(x)) + 1e-9) * peak


def brown(n):
    b = np.cumsum(rng.standard_normal(n))
    return b - lowpass(b, 0.5)


def olas():
    base = lowpass(brown(len(t)), 900)
    swell = 0.55 + 0.45 * np.sin(2 * np.pi * t / 8.0) ** 2  # una ola cada ~8 s
    foam = bandpass(rng.standard_normal(len(t)), 1500, 5000) * np.clip(np.sin(2 * np.pi * t / 8.0 + 0.6), 0, 1) ** 4
    return normalize(base * swell + 0.15 * foam)


def brisa():
    noise = rng.standard_normal(len(t))
    gust = 0.5 + 0.5 * np.sin(2 * np.pi * t / 6.0 + np.sin(2 * np.pi * t / 11.0))
    return normalize(bandpass(noise, 250, 1200) * gust, 0.45)


def campanas():
    x = np.zeros_like(t)
    notes = [523.25, 659.25, 783.99, 1046.5, 880.0]  # pentatónica suave
    for i, start in enumerate(np.arange(0.5, DUR - 3, 2.7)):
        f = notes[i % len(notes)]
        idx = t >= start
        tt = t[idx] - start
        x[idx] += np.sin(2 * np.pi * f * tt) * np.exp(-tt * 1.2) * 0.5
        x[idx] += np.sin(2 * np.pi * f * 2.01 * tt) * np.exp(-tt * 2.0) * 0.15
    pad = lowpass(brown(len(t)), 300) * 0.08
    return normalize(x + pad, 0.5)


def lluvia():
    hiss = bandpass(rng.standard_normal(len(t)), 2000, 8000) * 0.25
    drops = np.zeros_like(t)
    for pos in rng.integers(0, len(t) - 400, size=DUR * 40):
        drops[pos:pos + 400] += np.exp(-np.arange(400) / 60.0) * rng.uniform(0.2, 1.0)
    drops = bandpass(drops * rng.standard_normal(len(t)), 800, 6000)
    return normalize(hiss + drops, 0.45)


def save(name, signal):
    signal = seamless(signal)
    with tempfile.TemporaryDirectory() as tmp:
        wav = os.path.join(tmp, name + ".wav")
        wavfile.write(wav, SR, (signal * 32767).astype(np.int16))
        out = os.path.join(OUT, name + ".ogg")
        subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", wav, "-c:a", "libvorbis", "-q:a", "2", out], check=True)
        print(out, os.path.getsize(out), "bytes")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    save("sonido_olas", olas())
    save("sonido_brisa", brisa())
    save("sonido_campanas", campanas())
    save("sonido_lluvia", lluvia())

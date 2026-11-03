"""
Recorta a Muni desde el original del equipo (design-originals/muni/muni-original-sin-recortar.jpg)
sin alterar la ilustración: solo se elimina el fondo exterior al borde blanco tipo sticker.
Uso: python tools/recortar_muni.py
"""
import os
import numpy as np
from PIL import Image, ImageFilter
from scipy import ndimage

ROOT = os.path.join(os.path.dirname(__file__), "..")
SRC = os.path.join(ROOT, "design-originals", "muni", "muni-original-sin-recortar.jpg")
OUT = os.path.join(ROOT, "app", "src", "main", "res", "drawable", "img_muni_feelings.png")

im = Image.open(SRC).convert("RGB")
a = np.asarray(im).astype(np.int16)
r, g, b = a[..., 0], a[..., 1], a[..., 2]

# Candidatos a fondo: el turquesa del círculo de fondo (y su sombra) y la habitación cálida de las esquinas.
# El borde blanco tipo sticker (R≥225, G y B≈255) queda fuera, así que separa a Muni del fondo.
teal = np.array([157, 228, 221])
dist_teal = np.sqrt(((a - teal) ** 2).sum(axis=-1))
warm_room = (b < r - 4)
candidate_bg = (dist_teal < 45) | warm_room

# Fondo = todo lo no-blanco conectado con los bordes de la imagen
labels, _ = ndimage.label(candidate_bg)
border_labels = set(np.unique(np.concatenate([labels[0], labels[-1], labels[:, 0], labels[:, -1]]))) - {0}
background = np.isin(labels, list(border_labels))

# Muni = el componente opaco que contiene el centro (descarta arcos blancos sueltos del círculo de fondo)
fg = ~background
fg_labels, _ = ndimage.label(fg)
center_label = fg_labels[a.shape[0] // 2, a.shape[1] // 2]
mask = fg_labels == center_label
mask = ndimage.binary_fill_holes(mask)

alpha = Image.fromarray((mask * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(0.8))
rgba = im.convert("RGBA")
rgba.putalpha(alpha)
bbox = Image.fromarray((mask * 255).astype(np.uint8)).getbbox()
pad = 6
crop = rgba.crop((max(bbox[0] - pad, 0), max(bbox[1] - pad, 0), min(bbox[2] + pad, im.width), min(bbox[3] + pad, im.height)))
crop.save(OUT, optimize=True)
print("guardado", OUT, crop.size)

"""
Integra la ilustración original de la isla sin redibujarla:
1) quita la franja gris de la captura en el borde derecho,
2) vuelve transparente el fondo plano (para que se funda con el cielo de día, atardecer o noche),
3) difumina el borde inferior, donde el agua quedó cortada en la captura original.
Uso: python tools/integrar_isla.py  (parte de design-originals/isla/island_illustration_original.png)
"""
import os
import numpy as np
from PIL import Image
from scipy import ndimage

ROOT = os.path.join(os.path.dirname(__file__), "..")
SRC = os.path.join(ROOT, "design-originals", "isla", "island_illustration_original.png")
OUT = os.path.join(ROOT, "app", "src", "main", "res", "drawable", "island_illustration.png")

im = Image.open(SRC).convert("RGBA")
a = np.asarray(im).astype(np.int16)
sat = a[..., :3].max(-1) - a[..., :3].min(-1)
gray_cols = np.where(sat.mean(0) < 1.0)[0]
right = gray_cols.min() if len(gray_cols) and gray_cols.min() > a.shape[1] * 0.8 else a.shape[1]
a = a[:, :right]

bg_color = np.array([231, 240, 241])
near_bg = np.sqrt(((a[..., :3] - bg_color) ** 2).sum(-1)) < 14
labels, _ = ndimage.label(near_bg)
edge = set(np.unique(np.concatenate([labels[0], labels[:, 0], labels[:, -1]]))) - {0}
background = np.isin(labels, list(edge))
alpha = np.where(background, 0, 255).astype(np.float32)
alpha = ndimage.gaussian_filter(alpha, 0.7)

fade_rows = 16
h = a.shape[0]
for i in range(fade_rows):
    alpha[h - fade_rows + i] *= 1 - (i + 1) / (fade_rows + 1)

out = a.astype(np.uint8).copy()
out[..., 3] = np.clip(alpha, 0, 255).astype(np.uint8)
Image.fromarray(out, "RGBA").save(OUT, optimize=True)
print("guardado", OUT, out.shape[1], "x", out.shape[0])

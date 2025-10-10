#!/usr/bin/env python3
"""
Generate pixelated weather icon for the weather widget app.
Based on a partly cloudy weather icon with pixel art style.
"""

import struct
import zlib
import os


def create_png_pixel(width, height, pixels):
    """Create a simple PNG file from pixel data"""

    def png_pack(png_tag, data):
        chunk_head = png_tag
        return (
            struct.pack("!I", len(data))
            + chunk_head
            + data
            + struct.pack("!I", 0xFFFFFFFF & zlib.crc32(chunk_head + data))
        )

    png = b"\x89PNG\r\n\x1a\n"
    png += png_pack(b"IHDR", struct.pack("!2I5B", width, height, 8, 6, 0, 0, 0))

    raw_data = b""
    for row in pixels:
        raw_data += b"\x00"
        raw_data += row

    png += png_pack(b"IDAT", zlib.compress(raw_data, 9))
    png += png_pack(b"IEND", b"")

    return png


def create_pixelated_weather_icon(size):
    """Create a pixelated weather icon (sun with cloud)"""
    # Use 32x32 pixel grid for design
    grid_size = 32
    pixel_size = size // grid_size

    # Colors (RGBA) - amber/golden sun with white/gray clouds on black background
    transparent = b"\x00\x00\x00\x00"
    black = b"\x00\x00\x00\xff"
    amber = b"\xff\xae\x00\xff"
    gold = b"\xff\xd7\x00\xff"
    light_gold = b"\xff\xf4\xcc\xff"
    white = b"\xff\xff\xff\xff"
    light_gray = b"\xe0\xe0\xe0\xff"
    gray = b"\xc0\xc0\xc0\xff"
    dark_gray = b"\x90\x90\x90\xff"

    # Create pixel grid
    grid = [[transparent for _ in range(grid_size)] for _ in range(grid_size)]

    # Draw circular black background
    center = grid_size / 2
    radius = grid_size / 2
    for y in range(grid_size):
        for x in range(grid_size):
            dx = x - center + 0.5
            dy = y - center + 0.5
            if (dx * dx + dy * dy) <= (radius * radius):
                grid[y][x] = black

    def set_pixel(x, y, color):
        """Set a pixel if in bounds"""
        if 0 <= x < grid_size and 0 <= y < grid_size:
            grid[y][x] = color

    def fill_rect(x, y, w, h, color):
        """Fill a rectangle with color"""
        for i in range(h):
            for j in range(w):
                set_pixel(x + j, y + i, color)

    # Draw sun in upper left (pixelated)
    sun_x, sun_y = 10, 8

    # Sun rays (short pixel lines)
    fill_rect(sun_x, sun_y - 3, 1, 2, amber)  # top
    fill_rect(sun_x, sun_y + 3, 1, 2, amber)  # bottom
    fill_rect(sun_x - 3, sun_y, 2, 1, amber)  # left
    fill_rect(sun_x + 3, sun_y, 2, 1, amber)  # right
    set_pixel(sun_x - 2, sun_y - 2, amber)  # top-left
    set_pixel(sun_x + 2, sun_y - 2, amber)  # top-right
    set_pixel(sun_x - 2, sun_y + 2, amber)  # bottom-left
    set_pixel(sun_x + 2, sun_y + 2, amber)  # bottom-right

    # Sun core (3x3 pixels with gradient)
    set_pixel(sun_x - 1, sun_y - 1, gold)
    set_pixel(sun_x, sun_y - 1, gold)
    set_pixel(sun_x + 1, sun_y - 1, gold)
    set_pixel(sun_x - 1, sun_y, gold)
    set_pixel(sun_x, sun_y, light_gold)
    set_pixel(sun_x + 1, sun_y, gold)
    set_pixel(sun_x - 1, sun_y + 1, gold)
    set_pixel(sun_x, sun_y + 1, gold)
    set_pixel(sun_x + 1, sun_y + 1, gold)

    # Draw cloud (pixelated style - simplified from original SVG)
    cloud_x, cloud_y = 12, 16

    # Cloud outline and fill (asymmetric, puffy shape)
    # Top of cloud
    fill_rect(cloud_x + 2, cloud_y - 3, 6, 1, white)
    fill_rect(cloud_x + 1, cloud_y - 2, 8, 1, white)
    fill_rect(cloud_x, cloud_y - 1, 10, 1, white)

    # Middle of cloud
    fill_rect(cloud_x - 1, cloud_y, 12, 1, white)
    fill_rect(cloud_x - 1, cloud_y + 1, 12, 1, white)
    fill_rect(cloud_x - 1, cloud_y + 2, 13, 1, white)

    # Bottom of cloud (wider)
    fill_rect(cloud_x - 1, cloud_y + 3, 14, 1, white)
    fill_rect(cloud_x, cloud_y + 4, 13, 1, white)
    fill_rect(cloud_x + 1, cloud_y + 5, 11, 1, white)

    # Add some shading to cloud (light gray on bottom/right)
    fill_rect(cloud_x + 8, cloud_y - 1, 2, 1, light_gray)
    fill_rect(cloud_x + 9, cloud_y, 2, 1, light_gray)
    fill_rect(cloud_x + 9, cloud_y + 1, 2, 1, light_gray)
    fill_rect(cloud_x + 10, cloud_y + 2, 2, 1, light_gray)
    fill_rect(cloud_x + 11, cloud_y + 3, 2, 1, light_gray)
    fill_rect(cloud_x + 11, cloud_y + 4, 2, 1, light_gray)
    fill_rect(cloud_x + 10, cloud_y + 5, 2, 1, light_gray)

    # Add darker shading (gray)
    set_pixel(cloud_x + 10, cloud_y + 4, gray)
    set_pixel(cloud_x + 11, cloud_y + 4, gray)
    fill_rect(cloud_x + 9, cloud_y + 5, 1, 1, gray)
    fill_rect(cloud_x + 10, cloud_y + 5, 1, 1, gray)

    # Convert grid to actual pixel data
    rows = []
    for grid_y in range(grid_size):
        row_data = b""
        for grid_x in range(grid_size):
            # Replicate each grid pixel to fill the actual pixel size
            for _ in range(pixel_size):
                row_data += grid[grid_y][grid_x]
        # Replicate the entire row for pixel_size times
        for _ in range(pixel_size):
            rows.append(row_data)

    return create_png_pixel(size, size, rows)


# Generate icons in different sizes
sizes = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

base_path = "/Users/brsg/Projects/weatherwidget/weatherwidget/android/app/src/main/res"

for folder, size in sizes.items():
    folder_path = os.path.join(base_path, folder)
    os.makedirs(folder_path, exist_ok=True)

    png_data = create_pixelated_weather_icon(size)

    # Create both regular and round icons
    with open(os.path.join(folder_path, "ic_launcher.png"), "wb") as f:
        f.write(png_data)

    with open(os.path.join(folder_path, "ic_launcher_round.png"), "wb") as f:
        f.write(png_data)

    print(f"Created {folder}/ic_launcher.png and ic_launcher_round.png ({size}x{size})")

print("\nAll pixelated weather icons generated successfully!")

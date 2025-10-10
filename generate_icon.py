#!/usr/bin/env python3
"""
Generate pixel art sun icon for the weather widget app.
Creates PNG files in various sizes for Android launcher icons.
"""

# Check if we can create the icon using basic libraries
try:
    # Try to use simple image generation without external dependencies
    import struct

    def create_png_pixel(width, height, pixels):
        """Create a simple PNG file from pixel data"""
        import zlib

        def png_pack(png_tag, data):
            chunk_head = png_tag
            return (
                struct.pack("!I", len(data))
                + chunk_head
                + data
                + struct.pack("!I", 0xFFFFFFFF & zlib.crc32(chunk_head + data))
            )

        # PNG header
        png = b"\x89PNG\r\n\x1a\n"

        # IHDR chunk
        png += png_pack(b"IHDR", struct.pack("!2I5B", width, height, 8, 6, 0, 0, 0))

        # IDAT chunk - compress the pixel data
        raw_data = b""
        for row in pixels:
            raw_data += b"\x00"  # filter type
            raw_data += row

        png += png_pack(b"IDAT", zlib.compress(raw_data, 9))

        # IEND chunk
        png += png_pack(b"IEND", b"")

        return png

    def create_sun_icon(size):
        """Create a pixel art sun icon at the specified size"""
        # Calculate pixel block size
        block_size = size // 16

        # Colors (RGBA)
        transparent = b"\x00\x00\x00\x00"
        black = b"\x00\x00\x00\xff"
        amber = b"\xff\xae\x00\xff"
        gold = b"\xff\xd7\x00\xff"
        light = b"\xff\xf4\xcc\xff"

        # Create pixel grid with transparent background
        grid = [[transparent for _ in range(size)] for _ in range(size)]

        # Draw circular black background
        center = size / 2
        radius = size / 2
        for y in range(size):
            for x in range(size):
                dx = x - center + 0.5
                dy = y - center + 0.5
                if (dx * dx + dy * dy) <= (radius * radius):
                    grid[y][x] = black

        def fill_rect(x, y, w, h, color):
            """Fill a rectangle with color"""
            for i in range(y, min(y + h, size)):
                for j in range(x, min(x + w, size)):
                    if 0 <= i < size and 0 <= j < size:
                        grid[i][j] = color

        # Draw rays
        fill_rect(7 * block_size, block_size, 2 * block_size, block_size, amber)  # top
        fill_rect(
            3 * block_size, 3 * block_size, block_size, block_size, amber
        )  # top-left outer
        fill_rect(
            4 * block_size, 4 * block_size, block_size, block_size, amber
        )  # top-left inner
        fill_rect(
            12 * block_size, 3 * block_size, block_size, block_size, amber
        )  # top-right outer
        fill_rect(
            11 * block_size, 4 * block_size, block_size, block_size, amber
        )  # top-right inner
        fill_rect(block_size, 7 * block_size, block_size, 2 * block_size, amber)  # left
        fill_rect(
            14 * block_size, 7 * block_size, block_size, 2 * block_size, amber
        )  # right
        fill_rect(
            3 * block_size, 12 * block_size, block_size, block_size, amber
        )  # bottom-left outer
        fill_rect(
            4 * block_size, 11 * block_size, block_size, block_size, amber
        )  # bottom-left inner
        fill_rect(
            12 * block_size, 12 * block_size, block_size, block_size, amber
        )  # bottom-right outer
        fill_rect(
            11 * block_size, 11 * block_size, block_size, block_size, amber
        )  # bottom-right inner
        fill_rect(
            7 * block_size, 14 * block_size, 2 * block_size, block_size, amber
        )  # bottom

        # Draw sun core - outer (amber)
        fill_rect(6 * block_size, 5 * block_size, block_size, block_size, amber)
        fill_rect(7 * block_size, 4 * block_size, 2 * block_size, block_size, amber)
        fill_rect(9 * block_size, 5 * block_size, block_size, block_size, amber)
        fill_rect(5 * block_size, 6 * block_size, block_size, block_size, amber)
        fill_rect(10 * block_size, 6 * block_size, block_size, block_size, amber)
        fill_rect(5 * block_size, 7 * block_size, block_size, 2 * block_size, amber)
        fill_rect(10 * block_size, 7 * block_size, block_size, 2 * block_size, amber)
        fill_rect(5 * block_size, 9 * block_size, block_size, block_size, amber)
        fill_rect(10 * block_size, 9 * block_size, block_size, block_size, amber)
        fill_rect(6 * block_size, 10 * block_size, block_size, block_size, amber)
        fill_rect(7 * block_size, 11 * block_size, 2 * block_size, block_size, amber)
        fill_rect(9 * block_size, 10 * block_size, block_size, block_size, amber)

        # Draw sun core - middle (gold)
        fill_rect(6 * block_size, 6 * block_size, block_size, block_size, gold)
        fill_rect(7 * block_size, 5 * block_size, 2 * block_size, block_size, gold)
        fill_rect(9 * block_size, 6 * block_size, block_size, block_size, gold)
        fill_rect(6 * block_size, 7 * block_size, block_size, 2 * block_size, gold)
        fill_rect(9 * block_size, 7 * block_size, block_size, 2 * block_size, gold)
        fill_rect(6 * block_size, 9 * block_size, block_size, block_size, gold)
        fill_rect(7 * block_size, 10 * block_size, 2 * block_size, block_size, gold)
        fill_rect(9 * block_size, 9 * block_size, block_size, block_size, gold)

        # Draw sun core - center (light)
        fill_rect(7 * block_size, 6 * block_size, 2 * block_size, block_size, light)
        fill_rect(7 * block_size, 7 * block_size, 2 * block_size, 2 * block_size, light)
        fill_rect(7 * block_size, 9 * block_size, 2 * block_size, block_size, light)

        # Convert grid to PNG row data
        rows = []
        for row in grid:
            row_data = b"".join(row)
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

    import os

    base_path = (
        "/Users/brsg/Projects/weatherwidget/weatherwidget/android/app/src/main/res"
    )

    for folder, size in sizes.items():
        folder_path = os.path.join(base_path, folder)
        os.makedirs(folder_path, exist_ok=True)

        png_data = create_sun_icon(size)

        with open(os.path.join(folder_path, "ic_launcher.png"), "wb") as f:
            f.write(png_data)

        print(f"Created {folder}/ic_launcher.png ({size}x{size})")

    print("\nAll icons generated successfully!")

except Exception as e:
    print(f"Error: {e}")
    print("\nFalling back to manual icon creation...")
    print("Please install PIL: pip3 install Pillow")

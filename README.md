# Reach
Very accurate reach check using transactions with `Pledge` to verify when packets are received on the client.
Assuming optimal conditions, this reach check can detect anything above `3.00075` attack range.
This is due to the 'fast math' feature in Optifine causing a small error and can be removed if support is not needed.

# Important Notes
- This reach check only works on Minecraft 1.8 and does not support any versions above or below.
- I might have made a small mistake somewhere, so I don't guarantee there aren't any false flags. Feel free to open a pull request if you find an error.

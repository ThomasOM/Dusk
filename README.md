# Reach
Accurate reach check using transactions with [Pledge](https://github.com/ThomasOM/Pledge) to verify when packets are received on the client.
Assuming optimal conditions, this reach check can detect anything above an attack range of `3.00075`
The small error is due to the 'fast math' feature in Optifine and can be removed if support is not needed.

# Important Notes
- This reach check only works on Minecraft 1.8 and does not support any versions above or below
- The check has been tested for about ~20 minutes locally using clumsy and an autoclicker without any falses
- Not sure if I missed something... If I did, feel free to open an issue or a pull request!

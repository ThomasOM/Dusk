# Dusk
Server-side anticheat essentials for Minecraft 1.8
Uses transactions to track client state with [Pledge](https://github.com/ThomasOM/Pledge)

# Features
- Highly accurate range check detecting the lowest reach possible, anything above `3.00075` (Small error due to fast math)
- Optimal timing check detecting when users run their game faster than the server time
- Lag independent, users with bad connection do not cause any checks to false

# Important Notes
- This is only functional for Minecraft 1.8 and does not support any versions above or below
- All checks have been tested locally by using [clumsy](https://github.com/jagt/clumsy) to simulate network lag
- Not sure if I missed something... If I did, feel free to open an issue or a pull request!

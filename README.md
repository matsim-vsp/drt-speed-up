# drt-speed-up project

An attempt to speed up a drt simulation with mode choice.

(1) get some drt related statistics, e.g. average beeline speed, from a normal drt simulation,
(2) modify each person's selected plan before the mobsim starts and change the mode from 'drt' to 'drt_teleportation',
(3) teleport these agents instead of running the drt module and use the statistics from step (1),
(4) undo the plan modification and set the mode back from 'drt_teleportation' to 'drt' after the mobsim ends,
(5) repeat step (2)-(4) for some iterations and let agents adjust their mode of transportation,
(6) update the drt related statistics after some iterations and run a 'normal' drt simulation without modifying the plans.
Goto (1) and repeat the cycle several times
(7) Switch of the drt speed up mechanism and run a normal drt simulation for the final iterations

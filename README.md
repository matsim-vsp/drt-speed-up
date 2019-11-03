# drt-speed-up project

An attempt to speed up a simulation run with drt and mode choice.

(1) get some drt related statistics, e.g. average beeline speed, from a normal drt simulation,

(2) modify each person's selected plan before the mobsim starts and change the mode from 'drt' to 'drt_teleportation',

(3) teleport these agents instead of running the drt module and use the statistics from step (1) (also compute the fare and a penalty for teleported trips outside the drt service area),

(4) undo the plan modification and set the mode back from 'drt_teleportation' to 'drt' after the mobsim ends,

(5) repeat step (2)-(4) for some iterations and let agents adjust their mode of transportation,

(6) update the drt related statistics after some iterations and run a 'normal' drt simulation without modifying the plans.

Goto (1)

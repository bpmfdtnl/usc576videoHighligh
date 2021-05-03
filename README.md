## usc576videoHighlight

The purpose of this project is to take a long video and output the highlight version of that video. This can be useful when generating thumbnails or highlights.

## To Run
Run the main function in MediaPlayer/CreateHighLight.

## Modules

### Media Player

Takes rgb files directory and a .wav file to play the short video.

### ShotsSelects

Generate an array containing the timestamp of each new shot in the long video, preparation for the evaluation step.

### ShotsMerger

Receives evaluation score of each shot from a python program, select the most valuable shots to merge and edit the corresponding .wav file.


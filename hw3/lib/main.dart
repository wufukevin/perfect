import 'package:flutter/material.dart';
import 'package:flutter/services.dart';


void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Video',
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Video'),
          backgroundColor: Colors.deepOrange[700],
        ),
        body: const TextureDemo(),
      ),
    );
  }
}

class TextureDemo extends StatefulWidget{
  const TextureDemo({Key? key}) : super(key: key);

  @override
  _TextureDemoState createState() => _TextureDemoState();
}

class _TextureDemoState extends State<TextureDemo>{
  // transfer message about texture ID
  var videoPlugin = const MethodChannel("VideoCall");
  // listen event for progress bar
  var progressChannel = const EventChannel('VideoProgress');
  // transfer message about seek video
  var seekChannel = const MethodChannel('Seek');

  late int texTureId;
  late double fileDuration;

  bool playController = true;
  double currentSliderValue = 0;
  bool isSeeking = false;

  @override
  void initState() {
    super.initState();
    init();
  }

  init() async {
    // debugPrint('kevin_hw3 initial TextureDemo');

    var initResult = await videoPlugin.invokeMethod("initial Video");
    texTureId = initResult['textureID'];
    fileDuration = initResult['fileDuration'];
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    // update progress
    progressChannel.receiveBroadcastStream().listen(
            (dynamic currentProgress) {
          setState(() {
            if(!isSeeking){
              currentSliderValue = currentProgress;
            }
          });
        },
        onError: (dynamic currentProgress){}
    );

    return Scaffold(
        // backgroundColor: Colors.grey[900],
        backgroundColor: Colors.white,
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children:[
              AspectRatio(
                  aspectRatio: 16/20,
                  child: Texture(textureId: texTureId,)
              ),
              Slider(
                  value: currentSliderValue,
                  // divisions: 100,
                  min: 0,
                  max: fileDuration,
                  label: toClockType(currentSliderValue),
                  onChangeStart: (startValue) {
                    setState(() {
                      isSeeking = true;
                    });
                  },
                  onChangeEnd: (endValue) {
                    seekAndJumpTo(endValue);
                    setState(() {
                      isSeeking = false;
                    });
                  },
                  onChanged: (value) {
                    setState(() {
                      currentSliderValue = value;
                    });
                  }
              ),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(toClockType(currentSliderValue)),
                  const Text("  /  "),
                  Text(toClockType(fileDuration)),
                ],
              ),
              FloatingActionButton(
                backgroundColor: Colors.white24,
                highlightElevation: 0,
                onPressed: () async {
                  if (playController){
                    await videoPlugin.invokeMethod("stop Video");
                  } else {
                    await videoPlugin.invokeMethod("play Video");
                  }
                  setState(() {
                    if (playController){
                      playController = false;
                    } else {
                      playController = true;
                    }
                  });
                },
                child: Icon(
                  playController ? Icons.pause: Icons.play_arrow,
                ),
              ),
            ],
          ),
        )
    );
  }
  
  seekAndJumpTo(double time) async {
    await seekChannel.invokeMethod('seek video', {'time': time});
  }

  String toClockType(double time) {
    var min = time~/60;
    var sec = (time%60).toInt();
    var zero = (sec>9)? '' : '0';
    
    return '$min : $zero$sec';
  }
}

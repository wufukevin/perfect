import 'dart:developer';
import 'dart:math';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:video_player/video_player.dart';


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
        body: TextureDemo(),
      ),
    );
  }
}



class VideoPlayerDemo extends StatefulWidget{
  @override
  State<VideoPlayerDemo> createState() => _VideoPlayerDemoState();
}

class _VideoPlayerDemoState extends State<VideoPlayerDemo>{
  VideoPlayerController _controller= VideoPlayerController.network('https://flutter.github.io/assets-for-api-docs/assets/videos/butterfly.mp4',);
  var channel = MethodChannel('com.flutter.guide.MethodChannel');
  var _data;

  @override
  void initState() {
    _controller = VideoPlayerController.network('https://flutter.github.io/assets-for-api-docs/assets/videos/butterfly.mp4',);
    super.initState();
  }

  @override
  void dispose(){
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    _controller.initialize();
    return Scaffold(
      backgroundColor: Colors.grey[900],
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children:[
            AspectRatio(
              aspectRatio: 16/20,
              child: VideoPlayer(_controller)
            ),
            Spacer(),
            FloatingActionButton(
              backgroundColor: Colors.white24,
              highlightElevation: 0,
              onPressed: (){
                setState(() {
                  if (_controller.value.isPlaying){
                    _controller.pause();
                  } else {
                    _controller.play();
                  }
                });
              },
              child: Icon(
                _controller.value.isPlaying ? Icons.pause: Icons.play_arrow,
              ),
            ),
          ],
        ),
      )
    );
    throw UnimplementedError();
  }
  @override
  void debugFillProperties(DiagnosticPropertiesBuilder properties) {
    super.debugFillProperties(properties);
    properties.add(DiagnosticsProperty('_data', _data));
  }

}


class MethodChannelDemo extends StatefulWidget {
  const MethodChannelDemo({Key? key}) : super(key: key);

  @override
  _MethodChannelDemoState createState() => _MethodChannelDemoState();
}

class _MethodChannelDemoState extends State<MethodChannelDemo> {
  var channel = const MethodChannel('com.flutter.guide.MethodChannel');

  var _data;

  @override
  Widget build(BuildContext context) {
    debugPrint('kevin initial methodchanneldemo');
    return Scaffold(
      appBar: AppBar(),
      body: Column(
        children: [
          const SizedBox(
            height: 50,
          ),
          RaisedButton(
            child: const Text('发送数据到原生'),
            onPressed: () async {
              var randomAge = Random();
              var result = await channel
                  .invokeMethod('sendData', {'name': 'kevin', 'age': randomAge.nextInt(100)});
              var sentence = result['sentence'];
              setState(() {
                _data = sentence;
              });
            },
          ),
          Text('原生返回数据：$_data')
        ],
      ),
    );
  }

  @override
  void debugFillProperties(DiagnosticPropertiesBuilder properties) {
    super.debugFillProperties(properties);
    properties.add(DiagnosticsProperty('_data', _data));
  }
}

class TextureDemo extends StatefulWidget{
  @override
  _TextureDemoState createState() => _TextureDemoState();
}

class _TextureDemoState extends State<TextureDemo>{
  var videoPlugin = const MethodChannel("VideoCall");
  var texTureId = 123;
  var playController = true;
  double _currentSliderValue = 0;


  @override
  void initState() {
    super.initState();
    init();
  }

  init() async {
    debugPrint('kevin initial TextureDemo');
    texTureId = await videoPlugin.invokeMethod("initial Video");
    //??
    setState(() {});
  }
  @override
  Widget build(BuildContext context) {
    //   debugPrint('kevin build methodChannelDemo with textureID $texTureId');
    return Scaffold(
        backgroundColor: Colors.grey[900],
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children:[
              AspectRatio(
                  aspectRatio: 16/20,
                  child: Texture(textureId: texTureId,)
              ),
              Slider(
                  value: _currentSliderValue,
                  divisions: 100,
                  label: '$_currentSliderValue',
                  onChanged: (value) {
                    setState(() {
                      _currentSliderValue = value;
                    });
                  }
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
    throw UnimplementedError();
  }

}

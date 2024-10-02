import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:flutter/services.dart';
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as path;

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      home: HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  bool _isRecording = false;
  static const platform = MethodChannel('com.example.recorder_in_bg/recording');

  @override
  void initState() {
    super.initState();
    _checkPermissions();
  }

  Future<void> _checkPermissions() async {
    if (await Permission.microphone.request().isGranted) {
      // ได้รับอนุญาตแล้ว
    } else {
      // ไม่ได้รับอนุญาต
      showDialog(
        context: context,
        builder: (context) => AlertDialog(
          title: const Text('ต้องการสิทธิ์การใช้ไมโครโฟน'),
          content:
              const Text('แอปนี้ต้องการสิทธิ์การใช้ไมโครโฟนเพื่อบันทึกเสียง'),
          actions: [
            TextButton(
              child: const Text('ตกลง'),
              onPressed: () => Navigator.of(context).pop(),
            ),
          ],
        ),
      );
    }
  }

  Future<void> _toggleRecording() async {
    if (await Permission.microphone.isGranted) {
      setState(() {
        _isRecording = !_isRecording;
      });
      try {
        if (_isRecording) {
          final directory = await getDownloadsDirectory();
          String fileName =
              'audio_${DateTime.now().millisecondsSinceEpoch}.m4a';
          String pathname = path.join(directory!.path, fileName);
          await platform.invokeMethod('startRecording', {'pathname': pathname});
        } else {
          await platform.invokeMethod('stopRecording');
        }
      } on PlatformException catch (e) {
        print("Failed to toggle recording: '${e.message}'.");
      }
    } else {
      _checkPermissions();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('บันทึกเสียง')),
      body: Center(
        child: ElevatedButton(
          onPressed: _toggleRecording,
          child: Text(_isRecording ? 'หยุดบันทึก' : 'เริ่มบันทึก'),
        ),
      ),
    );
  }
}

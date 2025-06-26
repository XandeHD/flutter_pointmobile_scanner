import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Leitor de Códigos',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const BarcodeListScreen(),
    );
  }
}

class BarcodeListScreen extends StatefulWidget {
  const BarcodeListScreen({Key? key}) : super(key: key);

  @override
  State<BarcodeListScreen> createState() => _BarcodeListScreenState();
}

class _BarcodeListScreenState extends State<BarcodeListScreen> {
  static const MethodChannel _channel = MethodChannel('scanner_channel');
  final List<String> _barcodes = [];

  @override
  void initState() {
    super.initState();
    _channel.setMethodCallHandler((call) async {
      if (call.method == "onBarcodeScanned") {
        final code = call.arguments as String;
        setState(() {
          _barcodes.insert(0, code); // adiciona no topo da lista
        });
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Códigos Lidos')),
      body: _barcodes.isEmpty
          ? const Center(child: Text('Nenhum código lido ainda.'))
          : ListView.builder(
              itemCount: _barcodes.length,
              itemBuilder: (context, index) {
                return ListTile(
                  leading: const Icon(Icons.qr_code),
                  title: Text(_barcodes[index]),
                );
              },
            ),
    );
  }
}

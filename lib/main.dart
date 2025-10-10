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
      title: 'Weather Widget',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
      ),
      home: const MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key});

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  static const platform = MethodChannel('com.example.weatherwidget/location');

  double? _latitude;
  double? _longitude;
  bool _isLoading = false;
  String? _errorMessage;

  final TextEditingController _latController = TextEditingController();
  final TextEditingController _lonController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _loadLocation();
  }

  @override
  void dispose() {
    _latController.dispose();
    _lonController.dispose();
    super.dispose();
  }

  Future<void> _loadLocation() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final result = await platform.invokeMethod('getLocation');
      if (result != null) {
        setState(() {
          _latitude = result['latitude'];
          _longitude = result['longitude'];
          _latController.text = _latitude?.toStringAsFixed(6) ?? '';
          _lonController.text = _longitude?.toStringAsFixed(6) ?? '';
          _isLoading = false;
        });
      } else {
        setState(() {
          _isLoading = false;
          _errorMessage = 'No location saved. Please refresh or set manually.';
        });
      }
    } on PlatformException catch (e) {
      setState(() {
        _isLoading = false;
        _errorMessage = 'Failed to load location: ${e.message}';
      });
    }
  }

  Future<void> _refreshLocation() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final result = await platform.invokeMethod('refreshLocation');
      setState(() {
        _latitude = result['latitude'];
        _longitude = result['longitude'];
        _latController.text = _latitude?.toStringAsFixed(6) ?? '';
        _lonController.text = _longitude?.toStringAsFixed(6) ?? '';
        _isLoading = false;
      });

      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Location refreshed from GPS')),
      );
    } on PlatformException catch (e) {
      setState(() {
        _isLoading = false;
        _errorMessage = 'Failed to refresh location: ${e.message}';
      });
    }
  }

  Future<void> _setLocation() async {
    final lat = double.tryParse(_latController.text);
    final lon = double.tryParse(_lonController.text);

    if (lat == null || lon == null) {
      setState(() {
        _errorMessage = 'Invalid latitude or longitude';
      });
      return;
    }

    if (lat < -90 || lat > 90) {
      setState(() {
        _errorMessage = 'Latitude must be between -90 and 90';
      });
      return;
    }

    if (lon < -180 || lon > 180) {
      setState(() {
        _errorMessage = 'Longitude must be between -180 and 180';
      });
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final result = await platform.invokeMethod('setLocation', {
        'latitude': lat,
        'longitude': lon,
      });

      setState(() {
        _latitude = result['latitude'];
        _longitude = result['longitude'];
        _isLoading = false;
      });

      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Location updated successfully')),
      );
    } on PlatformException catch (e) {
      setState(() {
        _isLoading = false;
        _errorMessage = 'Failed to set location: ${e.message}';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: const Text('Weather Widget'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Text(
              'Current Location',
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),

            // Display current location
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        const Icon(Icons.location_on, size: 20),
                        const SizedBox(width: 8),
                        Expanded(
                          child: _latitude != null && _longitude != null
                              ? Text(
                                  'Lat: ${_latitude!.toStringAsFixed(6)}\nLon: ${_longitude!.toStringAsFixed(6)}',
                                  style: const TextStyle(fontSize: 16),
                                )
                              : const Text(
                                  'No location set',
                                  style: TextStyle(
                                    fontSize: 16,
                                    color: Colors.grey,
                                  ),
                                ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 16),

            // Refresh button
            ElevatedButton.icon(
              onPressed: _isLoading ? null : _refreshLocation,
              icon: const Icon(Icons.refresh),
              label: const Text('Refresh from GPS'),
            ),

            const SizedBox(height: 24),

            const Text(
              'Manual Location Entry',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),

            // Latitude input
            TextField(
              controller: _latController,
              decoration: const InputDecoration(
                labelText: 'Latitude',
                hintText: 'e.g., 37.7749',
                border: OutlineInputBorder(),
                prefixIcon: Icon(Icons.north),
              ),
              keyboardType: const TextInputType.numberWithOptions(
                decimal: true,
                signed: true,
              ),
            ),

            const SizedBox(height: 16),

            // Longitude input
            TextField(
              controller: _lonController,
              decoration: const InputDecoration(
                labelText: 'Longitude',
                hintText: 'e.g., -122.4194',
                border: OutlineInputBorder(),
                prefixIcon: Icon(Icons.east),
              ),
              keyboardType: const TextInputType.numberWithOptions(
                decimal: true,
                signed: true,
              ),
            ),

            const SizedBox(height: 16),

            // Set location button
            ElevatedButton(
              onPressed: _isLoading ? null : _setLocation,
              child: const Text('Set Location'),
            ),

            const SizedBox(height: 16),

            // Error message
            if (_errorMessage != null)
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.red.shade50,
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: Colors.red.shade200),
                ),
                child: Row(
                  children: [
                    Icon(Icons.error_outline, color: Colors.red.shade700),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        _errorMessage!,
                        style: TextStyle(color: Colors.red.shade700),
                      ),
                    ),
                  ],
                ),
              ),

            // Loading indicator
            if (_isLoading)
              const Center(
                child: Padding(
                  padding: EdgeInsets.all(16.0),
                  child: CircularProgressIndicator(),
                ),
              ),

            const Spacer(),

            // Info text
            const Text(
              'This location will be used by the weather widget on your home screen.',
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 12, color: Colors.grey),
            ),
          ],
        ),
      ),
    );
  }
}

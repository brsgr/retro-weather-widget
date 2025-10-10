import 'package:flutter/material.dart';
import 'package:geocoding/geocoding.dart';

class LocationPickerScreen extends StatefulWidget {
  const LocationPickerScreen({super.key});

  @override
  State<LocationPickerScreen> createState() => _LocationPickerScreenState();
}

class _LocationPickerScreenState extends State<LocationPickerScreen> {
  final TextEditingController _searchController = TextEditingController();
  List<String> _filteredSuggestions = [];
  bool _isLoading = false;
  String? _errorMessage;

  // Major cities around the world for autocomplete
  static const List<String> _popularCities = [
    'New York, NY, USA',
    'Los Angeles, CA, USA',
    'Chicago, IL, USA',
    'Houston, TX, USA',
    'Phoenix, AZ, USA',
    'Philadelphia, PA, USA',
    'San Antonio, TX, USA',
    'San Diego, CA, USA',
    'Dallas, TX, USA',
    'San Jose, CA, USA',
    'Austin, TX, USA',
    'Jacksonville, FL, USA',
    'Fort Worth, TX, USA',
    'Columbus, OH, USA',
    'San Francisco, CA, USA',
    'Charlotte, NC, USA',
    'Indianapolis, IN, USA',
    'Seattle, WA, USA',
    'Denver, CO, USA',
    'Boston, MA, USA',
    'Portland, OR, USA',
    'Miami, FL, USA',
    'Atlanta, GA, USA',
    'Las Vegas, NV, USA',
    'London, UK',
    'Paris, France',
    'Tokyo, Japan',
    'Sydney, Australia',
    'Toronto, Canada',
    'Vancouver, Canada',
    'Montreal, Canada',
    'Berlin, Germany',
    'Madrid, Spain',
    'Rome, Italy',
    'Amsterdam, Netherlands',
    'Stockholm, Sweden',
    'Oslo, Norway',
    'Copenhagen, Denmark',
    'Dublin, Ireland',
    'Vienna, Austria',
    'Brussels, Belgium',
    'Zurich, Switzerland',
    'Singapore',
    'Hong Kong',
    'Seoul, South Korea',
    'Beijing, China',
    'Shanghai, China',
    'Mumbai, India',
    'Delhi, India',
    'Bangalore, India',
    'Mexico City, Mexico',
    'Buenos Aires, Argentina',
    'São Paulo, Brazil',
    'Rio de Janeiro, Brazil',
    'Moscow, Russia',
    'Istanbul, Turkey',
    'Dubai, UAE',
    'Cairo, Egypt',
    'Cape Town, South Africa',
    'Auckland, New Zealand',
  ];

  @override
  void initState() {
    super.initState();
    _searchController.addListener(_onSearchChanged);
  }

  @override
  void dispose() {
    _searchController.removeListener(_onSearchChanged);
    _searchController.dispose();
    super.dispose();
  }

  void _onSearchChanged() {
    final query = _searchController.text.toLowerCase();
    if (query.isEmpty) {
      setState(() {
        _filteredSuggestions = [];
      });
      return;
    }

    setState(() {
      _filteredSuggestions = _popularCities
          .where((city) => city.toLowerCase().contains(query))
          .take(10)
          .toList();
    });
  }

  Future<void> _selectLocation(String locationName) async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      List<Location> locations = await locationFromAddress(locationName);

      if (locations.isEmpty) {
        setState(() {
          _isLoading = false;
          _errorMessage = 'Location not found';
        });
        return;
      }

      final location = locations.first;

      if (!mounted) return;
      Navigator.of(context).pop({
        'latitude': location.latitude,
        'longitude': location.longitude,
        'name': locationName,
      });
    } catch (e) {
      setState(() {
        _isLoading = false;
        _errorMessage = 'Failed to find location: ${e.toString()}';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF000000),
      appBar: AppBar(
        title: const Text(
          'Search Location',
          style: TextStyle(fontFamily: 'CustomFont'),
        ),
        backgroundColor: const Color(0xFF000000),
      ),
      body: Column(
        children: [
          // Search input
          Container(
            padding: const EdgeInsets.all(16),
            color: const Color(0xFF262626),
            child: TextField(
              controller: _searchController,
              autofocus: true,
              style: const TextStyle(
                fontFamily: 'CustomFont',
                color: Color(0xFFFFFFFF),
                fontSize: 16,
              ),
              decoration: InputDecoration(
                hintText: 'Type city name...',
                hintStyle: const TextStyle(
                  fontFamily: 'CustomFont',
                  color: Color(0xFF888888),
                ),
                prefixIcon: const Icon(Icons.search, color: Color(0xFFFFAE00)),
                suffixIcon: _searchController.text.isNotEmpty
                    ? IconButton(
                        icon: const Icon(Icons.clear, color: Color(0xFF888888)),
                        onPressed: () {
                          _searchController.clear();
                        },
                      )
                    : null,
                filled: true,
                fillColor: const Color(0xFF000000),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(color: Color(0xFF262626)),
                ),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(color: Color(0xFF262626)),
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(
                    color: Color(0xFFFFAE00),
                    width: 2,
                  ),
                ),
              ),
            ),
          ),

          // Error message
          if (_errorMessage != null)
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(16),
              color: const Color(0xFF331111),
              child: Row(
                children: [
                  const Icon(Icons.error_outline, color: Color(0xFFFF5555)),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      _errorMessage!,
                      style: const TextStyle(
                        fontFamily: 'CustomFont',
                        color: Color(0xFFFF5555),
                      ),
                    ),
                  ),
                ],
              ),
            ),

          // Loading indicator
          if (_isLoading)
            const Padding(
              padding: EdgeInsets.all(16),
              child: CircularProgressIndicator(color: Color(0xFFFFAE00)),
            ),

          // Suggestions list
          Expanded(
            child:
                _filteredSuggestions.isEmpty && _searchController.text.isEmpty
                ? Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(
                          Icons.location_city,
                          size: 64,
                          color: const Color(0xFFFFAE00).withValues(alpha: 0.3),
                        ),
                        const SizedBox(height: 16),
                        const Text(
                          'Search for a city',
                          style: TextStyle(
                            fontFamily: 'CustomFont',
                            color: Color(0xFF888888),
                            fontSize: 16,
                          ),
                        ),
                        const SizedBox(height: 8),
                        const Padding(
                          padding: EdgeInsets.symmetric(horizontal: 32),
                          child: Text(
                            'Type a city name to see suggestions',
                            textAlign: TextAlign.center,
                            style: TextStyle(
                              fontFamily: 'CustomFont',
                              color: Color(0xFF666666),
                              fontSize: 12,
                            ),
                          ),
                        ),
                      ],
                    ),
                  )
                : _filteredSuggestions.isEmpty
                ? Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        const Icon(
                          Icons.search_off,
                          size: 64,
                          color: Color(0xFF666666),
                        ),
                        const SizedBox(height: 16),
                        const Text(
                          'No suggestions found',
                          style: TextStyle(
                            fontFamily: 'CustomFont',
                            color: Color(0xFF888888),
                            fontSize: 16,
                          ),
                        ),
                        const SizedBox(height: 8),
                        ElevatedButton(
                          onPressed: () =>
                              _selectLocation(_searchController.text),
                          child: const Text('Try searching anyway'),
                        ),
                      ],
                    ),
                  )
                : ListView.builder(
                    itemCount: _filteredSuggestions.length,
                    itemBuilder: (context, index) {
                      final suggestion = _filteredSuggestions[index];
                      return ListTile(
                        leading: const Icon(
                          Icons.location_on,
                          color: Color(0xFFFFAE00),
                        ),
                        title: Text(
                          suggestion,
                          style: const TextStyle(
                            fontFamily: 'CustomFont',
                            color: Color(0xFFFFFFFF),
                            fontSize: 16,
                          ),
                        ),
                        onTap: () => _selectLocation(suggestion),
                        tileColor: const Color(0xFF000000),
                        hoverColor: const Color(0xFF262626),
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }
}

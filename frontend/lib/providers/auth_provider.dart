import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class AuthProvider with ChangeNotifier {
  String? _token;
  final String _tokenKey = 'jwt_token'; // Key để lưu token
  bool _isAuthenticated = false;

  bool get isAuthenticated => _isAuthenticated;
  String? get token => _token;

  Future<void> login(String username, String password) async {
    try {
      final response = await http.post(
        Uri.parse('http://localhost:8000/token'),
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: {
          'username': username,
          'password': password,
        },
      );

      if (response.statusCode == 200) {
        final data = json.decode(response.body);
        _token = data['access_token'];
        _isAuthenticated = true;
        notifyListeners();
      } else {
        throw Exception('Failed to login');
      }
    } catch (e) {
      throw Exception('Failed to login: $e');
    }
  }

  Future<void> register(String username, String email, String password) async {
    try {
      final response = await http.post(
        Uri.parse('http://localhost:8000/api/users/register'),
        headers: {'Content-Type': 'application/json'},
        body: json.encode({
          'username': username,
          'email': email,
          'password': password,
        }),
      );

      if (response.statusCode == 200) {
        await login(username, password);
      } else {
        throw Exception('Failed to register');
      }
    } catch (e) {
      throw Exception('Failed to register: $e');
    }
  }

  Future<void> logout() async {
    _token = null;
    _isAuthenticated = false;
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_tokenKey); // Xóa token
    notifyListeners();
  }

  Future<void> setToken(String token) async {
    _token = token;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_tokenKey, token); // Lưu token
    notifyListeners();
  }

  // Thử tự động đăng nhập khi khởi động app
  Future<bool> tryAutoLogin() async {
    final prefs = await SharedPreferences.getInstance();
    if (!prefs.containsKey(_tokenKey)) {
      return false;
    }
    final extractedToken = prefs.getString(_tokenKey);
    if (extractedToken == null) {
       return false;
    }

    // TODO: Thêm logic kiểm tra token hết hạn nếu cần
    // Ví dụ: giải mã token và kiểm tra trường `exp`

    _token = extractedToken;
    _isAuthenticated = true;
    notifyListeners();
    return true;
  }
} 
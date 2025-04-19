import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'auth_provider.dart';

class Todo {
  final String id;
  String title;
  bool completed;
  final DateTime? createdAt;
  final DateTime? completedAt;

  Todo({
    required this.id,
    required this.title,
    this.completed = false,
    this.createdAt,
    this.completedAt,
  });

  factory Todo.fromJson(Map<String, dynamic> json) {
    return Todo(
      id: json['id'] as String? ?? '',
      title: json['title'] as String? ?? '',
      completed: json['completed'] as bool? ?? false,
      createdAt: json['createdAt'] != null ? DateTime.tryParse(json['createdAt']) : null,
      completedAt: json['completedAt'] != null ? DateTime.tryParse(json['completedAt']) : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'completed': completed,
    };
  }
}

class TodoProvider with ChangeNotifier {
  final AuthProvider _authProvider;
  List<Todo> _todos = [];
  bool _isDisposed = false;

  TodoProvider(this._authProvider, {List<Todo>? previousTodos}) {
    if (previousTodos != null) {
      _todos = previousTodos;
    }
  }

  @override
  void dispose() {
    _isDisposed = true;
    super.dispose();
  }

  void _safeNotifyListeners() {
    if (!_isDisposed) {
      notifyListeners();
    }
  }

  List<Todo> get todos => [..._todos];

  Future<void> fetchTodos() async {
    if (_authProvider.token == null) return;
    try {
      final response = await http.get(
        Uri.parse('http://localhost:8080/api/todos'),
        headers: {
          'Authorization': 'Bearer ${_authProvider.token}',
        },
      );

      if (response.statusCode == 200) {
        final List<dynamic> data = json.decode(response.body);
        _todos = data
            .map((item) => item is Map<String, dynamic> ? Todo.fromJson(item) : null)
            .where((todo) => todo != null)
            .cast<Todo>()
            .toList();
        _safeNotifyListeners();
      } else {
        if (response.statusCode == 401 || response.statusCode == 403) {
           _authProvider.logout();
        }
        print('Failed to fetch todos: ${response.statusCode}');
        _todos = [];
        _safeNotifyListeners();
      }
    } catch (e) {
      print('Error fetching todos: $e');
      _todos = [];
      _safeNotifyListeners();
    }
  }

  Future<void> addTodo(String title) async {
    if (_authProvider.token == null) return;
    try {
      final response = await http.post(
        Uri.parse('http://localhost:8080/api/todos'),
        headers: {
          'Authorization': 'Bearer ${_authProvider.token}',
          'Content-Type': 'application/json',
        },
        body: json.encode({'title': title}),
      );

      if (response.statusCode == 201) {
         try {
             final dynamic responseData = json.decode(response.body);
             if (responseData is Map<String, dynamic>) {
                 final todo = Todo.fromJson(responseData);
                 _todos.add(todo);
                 _safeNotifyListeners();
             } else {
                 print('Error adding todo: Invalid response format');
             }
          } catch (e) {
              print('Error decoding add response: $e');
          }
      } else {
         if (response.statusCode == 401 || response.statusCode == 403) {
           _authProvider.logout();
         }
         print('Failed to add todo: ${response.statusCode}');
      }
    } catch (e) {
      print('Error adding todo: $e');
    }
  }

  Future<void> updateTodo(String id, {String? newTitle, bool? newCompletedStatus}) async {
    if (_authProvider.token == null) return;
    final todoIndex = _todos.indexWhere((todo) => todo.id == id);
    if (todoIndex == -1) return;

    final todo = _todos[todoIndex];
    final oldTitle = todo.title;
    final oldStatus = todo.completed;

    bool changed = false;
    if (newTitle != null && newTitle != oldTitle) {
      todo.title = newTitle;
      changed = true;
    }
    if (newCompletedStatus != null && newCompletedStatus != oldStatus) {
      todo.completed = newCompletedStatus;
      changed = true;
    }

    if (changed) {
      _safeNotifyListeners();
    } else {
      return;
    }

    try {
      final Map<String, dynamic> updateData = {};
      if (newTitle != null) updateData['title'] = newTitle;
      if (newCompletedStatus != null) updateData['completed'] = newCompletedStatus;

      final response = await http.put(
        Uri.parse('http://localhost:8080/api/todos/$id'),
        headers: {
          'Authorization': 'Bearer ${_authProvider.token}',
          'Content-Type': 'application/json',
        },
        body: json.encode(updateData),
      );

      if (response.statusCode != 200) {
        todo.title = oldTitle;
        todo.completed = oldStatus;
        _safeNotifyListeners();
        if (response.statusCode == 401 || response.statusCode == 403) {
           _authProvider.logout();
        }
        print('Failed to update todo: ${response.statusCode}');
      } else {
          try {
              final dynamic responseData = json.decode(response.body);
              if (responseData is Map<String, dynamic>) {
                  final updatedTodoFromServer = Todo.fromJson(responseData);
                  _todos[todoIndex] = updatedTodoFromServer;
                   _safeNotifyListeners();
              } else {
                   print('Error updating todo: Invalid response format from server');
              }
          } catch(e) {
               print('Error decoding update response: $e');
          }
      }
    } catch (e) {
      todo.title = oldTitle;
      todo.completed = oldStatus;
      _safeNotifyListeners();
      print('Error updating todo: $e');
    }
  }

  Future<void> deleteTodo(String id) async {
    if (_authProvider.token == null) return;
    final existingTodoIndex = _todos.indexWhere((todo) => todo.id == id);
    if (existingTodoIndex == -1) return;
    Todo? existingTodo = _todos[existingTodoIndex];
    _todos.removeAt(existingTodoIndex);
    _safeNotifyListeners();

    try {
      final response = await http.delete(
        Uri.parse('http://localhost:8080/api/todos/$id'),
        headers: {
          'Authorization': 'Bearer ${_authProvider.token}',
        },
      );

      if (response.statusCode != 200 && response.statusCode != 204) {
        _todos.insert(existingTodoIndex, existingTodo);
        _safeNotifyListeners();
         if (response.statusCode == 401 || response.statusCode == 403) {
           _authProvider.logout();
         }
        print('Failed to delete todo: ${response.statusCode}');
      }
      existingTodo = null;
    } catch (e) {
      if (existingTodo != null) {
           _todos.insert(existingTodoIndex, existingTodo);
           _safeNotifyListeners();
      }
      print('Error deleting todo: $e');
    }
  }
} 
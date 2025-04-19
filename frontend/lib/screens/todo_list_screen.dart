import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import '../providers/auth_provider.dart';
import '../providers/todo_provider.dart';
import 'login_screen.dart';

class TodoListScreen extends StatefulWidget {
  static const routeName = '/todos';
  const TodoListScreen({super.key});

  @override
  State<TodoListScreen> createState() => _TodoListScreenState();
}

class _TodoListScreenState extends State<TodoListScreen> {
  final _todoController = TextEditingController();
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _loadTodos();
  }

  Future<void> _loadTodos() async {
    setState(() => _isLoading = true);
    try {
      if (mounted) {
        await Provider.of<TodoProvider>(context, listen: false).fetchTodos();
      }
    } finally {
      if (mounted) {
        setState(() => _isLoading = false);
      }
    }
  }

  Future<void> _addTodo() async {
    if (_todoController.text.isEmpty) return;
    final todoProvider = Provider.of<TodoProvider>(context, listen: false);
    await todoProvider.addTodo(_todoController.text);
    _todoController.clear();
  }

  Future<void> _showEditDialog(BuildContext context, Todo todo) async {
    final editController = TextEditingController(text: todo.title);
    return showDialog<void>(
      context: context,
      barrierDismissible: false,
      builder: (BuildContext dialogContext) {
        return AlertDialog(
          title: const Text('Sửa công việc'),
          content: TextField(
            controller: editController,
            decoration: const InputDecoration(hintText: "Nhập tiêu đề mới"),
            autofocus: true,
          ),
          actions: <Widget>[
            TextButton(
              child: const Text('Hủy'),
              onPressed: () {
                Navigator.of(dialogContext).pop();
              },
            ),
            TextButton(
              child: const Text('Lưu'),
              onPressed: () {
                final newTitle = editController.text;
                if (newTitle.isNotEmpty) {
                  Provider.of<TodoProvider>(context, listen: false)
                      .updateTodo(todo.id, newTitle: newTitle);
                  Navigator.of(dialogContext).pop();
                } else {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Tiêu đề không được để trống')),
                  );
                }
              },
            ),
          ],
        );
      },
    );
  }

  void _logout() {
    Provider.of<AuthProvider>(context, listen: false).logout();
    Navigator.of(context).pushNamedAndRemoveUntil(LoginScreen.routeName, (Route<dynamic> route) => false);
  }

  @override
  Widget build(BuildContext context) {
    final DateFormat formatter = DateFormat('HH:mm dd/MM/yyyy');

    return Scaffold(
      appBar: AppBar(
        title: const Text('Danh sách công việc'),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            tooltip: 'Đăng xuất',
            onPressed: _logout,
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: _loadTodos,
        child: _isLoading
            ? const Center(child: CircularProgressIndicator())
            : Column(
                children: [
                  Padding(
                    padding: const EdgeInsets.all(12.0),
                    child: Row(
                      children: [
                        Expanded(
                          child: TextField(
                            controller: _todoController,
                            decoration: const InputDecoration(
                              labelText: 'Thêm công việc mới',
                              border: OutlineInputBorder(),
                              contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                            ),
                            onSubmitted: (_) => _addTodo(),
                          ),
                        ),
                        const SizedBox(width: 8),
                        ElevatedButton.icon(
                          onPressed: _addTodo,
                          icon: const Icon(Icons.add),
                          label: const Text('Thêm'),
                          style: ElevatedButton.styleFrom(padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 16)),
                        ),
                      ],
                    ),
                  ),
                  Expanded(
                    child: Consumer<TodoProvider>(
                      builder: (context, todoProvider, child) {
                        final todos = todoProvider.todos;
                        if (todos.isEmpty && !_isLoading) {
                          return const Center(
                            child: Text('Chưa có công việc nào. Hãy thêm một việc mới!'),
                          );
                        }
                        return ListView.builder(
                          itemCount: todos.length,
                          itemBuilder: (context, index) {
                            final todo = todos[index];
                            String subtitleText = 'Tạo: ${todo.createdAt != null ? formatter.format(todo.createdAt!) : "-"}';
                            if (todo.completed && todo.completedAt != null) {
                              subtitleText += ' | HT: ${formatter.format(todo.completedAt!)}';
                            }

                            return Dismissible(
                              key: ValueKey(todo.id),
                              background: Container(
                                color: Colors.redAccent,
                                alignment: Alignment.centerRight,
                                padding: const EdgeInsets.only(right: 20),
                                child: const Icon(
                                  Icons.delete_sweep,
                                  color: Colors.white,
                                ),
                              ),
                              direction: DismissDirection.endToStart,
                              onDismissed: (_) {
                                Provider.of<TodoProvider>(context, listen: false)
                                    .deleteTodo(todo.id);
                                ScaffoldMessenger.of(context).showSnackBar(
                                  SnackBar(content: Text("${todo.title} đã được xóa")),
                                );
                              },
                              child: ListTile(
                                leading: Checkbox(
                                  value: todo.completed,
                                  onChanged: (bool? newValue) {
                                    Provider.of<TodoProvider>(context, listen: false)
                                        .updateTodo(todo.id, newCompletedStatus: newValue);
                                  },
                                ),
                                title: Text(
                                  todo.title,
                                  style: TextStyle(
                                    decoration: todo.completed ? TextDecoration.lineThrough : null,
                                    color: todo.completed ? Colors.grey[600] : null,
                                  ),
                                ),
                                subtitle: Text(subtitleText, style: const TextStyle(fontSize: 12, color: Colors.grey)),
                                trailing: IconButton(
                                  icon: const Icon(Icons.edit_note, color: Colors.blue),
                                  tooltip: 'Sửa công việc',
                                  onPressed: () => _showEditDialog(context, todo),
                                ),
                              ),
                            );
                          },
                        );
                      },
                    ),
                  ),
                ],
              ),
      ),
    );
  }

  @override
  void dispose() {
    _todoController.dispose();
    super.dispose();
  }
} 
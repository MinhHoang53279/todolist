import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:google_fonts/google_fonts.dart';
import 'screens/login_screen.dart';
import 'screens/todo_list_screen.dart';
import 'screens/register_screen.dart';
import 'providers/auth_provider.dart';
import 'providers/todo_provider.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthProvider()),
        ChangeNotifierProxyProvider<AuthProvider, TodoProvider>(
          create: (_) => TodoProvider(AuthProvider()),
          update: (context, authProvider, previousTodoProvider) {
            return TodoProvider(authProvider, previousTodos: previousTodoProvider?.todos);
          },
        ),
      ],
      child: Consumer<AuthProvider>(
        builder: (context, auth, _) {
          return MaterialApp(
            title: 'Todo App',
            theme: ThemeData(
              primarySwatch: Colors.blue,
              textTheme: GoogleFonts.robotoTextTheme(
                Theme.of(context).textTheme,
              ),
              useMaterial3: true,
            ),
            home: auth.isAuthenticated
                ? const TodoListScreen()
                : FutureBuilder(
                    future: auth.tryAutoLogin(),
                    builder: (ctx, authResultSnapshot) {
                      if (authResultSnapshot.connectionState == ConnectionState.waiting) {
                        return const Scaffold(body: Center(child: CircularProgressIndicator()));
                      } else {
                        return auth.isAuthenticated ? const TodoListScreen() : const LoginScreen();
                      }
                    },
                  ),
            routes: {
              LoginScreen.routeName: (ctx) => const LoginScreen(),
              RegisterScreen.routeName: (ctx) => const RegisterScreen(),
              TodoListScreen.routeName: (ctx) => const TodoListScreen(),
            },
          );
        },
      ),
    );
  }
} 
# 63CNTT-Mobile-Dev: LẬP TRÌNH ỨNG DỤNG MOBILE
> Sinh viên: Nguyễn Quốc Thái 63CNTT-CLC1
 ## Install:
 - Android Studio
 - Android 7.0 (Nougat API 24)
 - Java SE Development Kit (8u77)

 ***Quá trình thực hiện bài tập***
 ### Real Time Tracker
 - Dự án chia sẻ vị trí lẫn nhau trong gia đình,bạn bè.
 - TIến độ hoàn thành dự án
 ![](https://geps.dev/progress/60)
 ![image](https://media.discordapp.net/attachments/1013818251580551170/1164047275077668884/image.png)
 ### Firebase
 
 ![image](https://media.discordapp.net/attachments/1013818251580551170/1164052580993863700/image.png)
- code initial Authentication
 
    ```sh   
        private FirebaseAuth auth;
        auth = FirebaseAuth.getInstance();
    ```
# Nâng cấp layout
  - Thêm xử lý sự kiện
    *Sự kiện button `login`*
    ```java
        private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Toast.makeText(LoginAcitivity.this,"Login successfully!",Toast.LENGTH_LONG).show();
                startActivity(new Intent(LoginAcitivity.this,MainActivity.class));
                finish();
            }
        });
    }
    ```
    
    ***Khi người dùng nhập thông tin tài khoản đã tạo và rồi nhấn Log in***

![image](https://media.discordapp.net/attachments/1013818251580551170/1164056603734065162/image.png)
    
 * Sự kiện button `register`
 
    ```java
        private void registerUser(String email, String password, String firstname, String lastname) {
        if (!isButtonClicked) {
            isButtonClicked = true; // Đánh dấu rằng button đã được click
            // Thực hiện công việc cần thiết ở đây
            auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                @Override
                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                    try {
                        if (task.isSuccessful()) {
                            SignInMethodQueryResult result = task.getResult();
                            if (result.getSignInMethods() != null && result.getSignInMethods().size() > 0) {
                                // Tài khoản đã tồn tại
                                Toast.makeText(RegisterActivity.this, "Account already exists!", Toast.LENGTH_LONG).show();
                            } else {
                                Intent myIntent = new Intent(RegisterActivity.this, ConfirmActivity.class);
                                myIntent.putExtra("email", email);
                                myIntent.putExtra("password", password);
                                myIntent.putExtra("firstname", firstname);
                                myIntent.putExtra("lastname", lastname);
                                startActivity(myIntent);
                            }
                        } else {
                            // Xử lý lỗi nếu có
                            Toast.makeText(RegisterActivity.this,task.getException().toString(),Toast.LENGTH_LONG).show();
                            throw task.getException();
                        }
                    } catch (Exception e) {
                        // Xử lý trường hợp tắt mạng hoặc lỗi kết nối
                        Toast.makeText(RegisterActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    } finally {
                        isButtonClicked = false;
                    }
                }
            });
        }
    }
    }
    ```
     ![image](https://media.discordapp.net/attachments/1013818251580551170/1164056998103498793/image.png)

  *Sự kiện chuyển đổi qua lại giữa `Log in` và `Sign up` và ngược lại*
   ```java
    loginAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginAcitivity.class));
            }
        });
   ```
   ```java
   createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginAcitivity.this,RegisterActivity.class));
            }
        });
   ```
   
- Truyền tên, email,password qua ConfirmActivity để xác nhận lại lần cuối và chọn avatar
   ![image](https://media.discordapp.net/attachments/1013818251580551170/1164051821556404284/image.png)

   * Sự kiện tạo code `invite` 6 chữ số
   
   ```java
    private String generateCode() {
        Random r = new Random();
        int intcode = 100000 + r.nextInt(900000);
        String code = String.valueOf(intcode);
        return code;
    }
   ```
- Thêm map
   ![image](https://scontent.fdad1-2.fna.fbcdn.net/v/t1.15752-9/398366975_679159977647104_4476033544756709601_n.png?_nc_cat=106&ccb=1-7&_nc_sid=8cd0a2&_nc_eui2=AeHbalKKSDJiZ3ZhrbvuABsSGYPUgy87AlwZg9SDLzsCXKmrePSaNOTMZrc-DREEfSfXwvHV64rGcPH-zBaR-vTm&_nc_ohc=K5xAxYNRWwUAX8VXGrp&_nc_ht=scontent.fdad1-2.fna&oh=03_AdQMIiJnj6njEUidz53fQxwitqEROZCQmKTVwpqAPfJbuA&oe=6575F5EF)

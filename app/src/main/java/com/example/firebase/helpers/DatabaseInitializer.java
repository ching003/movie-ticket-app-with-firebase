package com.example.firebase.helpers;

import android.content.Context;
import android.util.Log;

import com.example.firebase.dal.AppDB;
import com.example.firebase.entities.Movie;
import com.example.firebase.entities.Theater;
import com.example.firebase.entities.Showtime;
import com.example.firebase.entities.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseInitializer {

    private static final String TAG = "DatabaseInitializer";
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void initializeSampleData(Context context) {
        AppDB database = AppDB.getInstance(context);

        executorService.execute(() -> {
            if (database.movieDAO().getAll().isEmpty()) {
                Log.d(TAG, "Initializing sample data...");
                
                insertSampleUsers(database);
                insertSampleMovies(database);
                insertSampleTheaters(database);
                insertSampleShowtimes(database);
                
                Log.d(TAG, "Sample data initialized successfully!");
            } else {
                Log.d(TAG, "Database already has data");
            }
        });
    }

    public static void insertSampleUsers(AppDB database) {
        User[] users = {
            new User("admin", "admin123", "Quản trị viên", "admin@movie.com", "0901234567"),
            new User("user1", "123456", "Nguyễn Văn A", "nguyenvana@gmail.com", "0912345678"),
            new User("user2", "123456", "Trần Thị B", "tranthib@gmail.com", "0923456789")
        };

        for (User user : users) {
            database.userDAO().insert(user);
        }
        Log.d(TAG, "Inserted " + users.length + " users");
    }

    public static void insertSampleMovies(AppDB database) {
        String posterUrl = "https://cdn2.fptshop.com.vn/unsafe/Uploads/images/tin-tuc/176627/Originals/poster-phim-hoat-hinh-1.jpg";
        
        Movie[] movies = {
            new Movie("Avengers: Endgame", 
                "Sau sự kiện của Infinity War, vũ trụ đang trong tình trạng hỗn loạn. Các siêu anh hùng còn sống sót tập hợp lại để đảo ngược những gì Thanos đã làm.", 
                "Hành động", 181, posterUrl, 8.4f, 2019),
            
            new Movie("Parasite", 
                "Câu chuyện về gia đình nghèo khó xâm nhập vào cuộc sống của gia đình giàu có thông qua những lời nói dối tinh vi.", 
                "Tâm lý", 132, posterUrl, 8.6f, 2019),
            
            new Movie("The Batman", 
                "Khi kẻ giết người nhắm vào giới tinh hoa Gotham với loạt âm mưu tàn độc, Batman phải điều tra thế giới ngầm.", 
                "Hành động", 176, posterUrl, 7.8f, 2022),
            
            new Movie("Spider-Man: No Way Home", 
                "Danh tính Spider-Man bị lộ, Peter Parker nhờ Doctor Strange giúp đỡ nhưng mọi thứ trở nên tồi tệ hơn.", 
                "Hành động", 148, posterUrl, 8.2f, 2021),
            
            new Movie("Oppenheimer", 
                "Câu chuyện về J. Robert Oppenheimer - người cha đẻ của bom nguyên tử và cuộc đời đầy mâu thuẫn của ông.", 
                "Tiểu sử", 180, posterUrl, 8.3f, 2023),
            
            new Movie("Barbie", 
                "Barbie và Ken khám phá thế giới thực sau khi bị trục xuất khỏi Barbie Land vì không hoàn hảo.", 
                "Hài", 114, posterUrl, 6.9f, 2023),
            
            new Movie("John Wick 4", 
                "John Wick tìm cách đánh bại High Table và giành lại tự do một lần và mãi mãi.", 
                "Hành động", 169, posterUrl, 7.7f, 2023),
            
            new Movie("The Shawshank Redemption", 
                "Hai tù nhân kết bạn qua nhiều năm, tìm thấy sự an ủi và cứu rỗi thông qua những hành động tử tế.", 
                "Tâm lý", 142, posterUrl, 9.3f, 1994),
            
            new Movie("Inception", 
                "Tên trộm chuyên nghiệp ăn cắp bí mật từ tiềm thức người khác khi họ đang mơ.", 
                "Khoa học viễn tưởng", 148, posterUrl, 8.8f, 2010),
            
            new Movie("The Godfather", 
                "Trùm mafia già chuyển giao quyền kiểm soát đế chế tội phạm cho con trai miễn cưỡng của mình.", 
                "Tội phạm", 175, posterUrl, 9.2f, 1972),
            
            new Movie("Dune", 
                "Paul Atreides du hành đến hành tinh nguy hiểm nhất vũ trụ để bảo vệ tương lai gia đình và người dân.", 
                "Khoa học viễn tưởng", 155, posterUrl, 8.0f, 2021),
            
            new Movie("Top Gun: Maverick", 
                "Sau hơn 30 năm phục vụ, Pete Mitchell quay lại để huấn luyện một nhóm phi công trẻ cho nhiệm vụ đặc biệt.", 
                "Hành động", 130, posterUrl, 8.3f, 2022)
        };

        for (Movie movie : movies) {
            database.movieDAO().insert(movie);
        }
        Log.d(TAG, "Inserted " + movies.length + " movies");
    }

    public static void insertSampleTheaters(AppDB database) {
        Theater[] theaters = {
            new Theater("CGV Vincom Center", "Quận 1", "72 Lê Thánh Tôn, P.Bến Nghé, Q.1, TP.HCM", "1900 6017", 8),
            new Theater("Galaxy Nguyễn Du", "Quận 1", "116 Nguyễn Du, P.Bến Thành, Q.1, TP.HCM", "1900 2224", 6),
            new Theater("Lotte Cinema Cộng Hòa", "Quận Tân Bình", "180 Cộng Hòa, P.12, Q.Tân Bình, TP.HCM", "1900 5454", 7),
            new Theater("BHD Star Bitexco", "Quận 1", "Tầng 3, Bitexco, 19-23 Nguyễn Huệ, Q.1, TP.HCM", "1900 2099", 10),
            new Theater("CGV Aeon Bình Tân", "Quận Bình Tân", "Tầng 3, AEON Mall, Số 1 Đường 17A, Q.Bình Tân, TP.HCM", "1900 6017", 9),
            new Theater("Galaxy Kinh Dương Vương", "Quận 6", "718 Kinh Dương Vương, P.12, Q.6, TP.HCM", "1900 2224", 5),
            new Theater("Lotte Cinema Thủ Đức", "Thủ Đức", "Tầng 3, Vincom Plaza, 216 Võ Văn Ngân, TP.Thủ Đức", "1900 5454", 8),
            new Theater("CGV Sư Vạn Hạnh", "Quận 10", "Tầng 6, Sư Vạn Hạnh Plaza, 11 Sư Vạn Hạnh, Q.10, TP.HCM", "1900 6017", 7)
        };

        for (Theater theater : theaters) {
            database.theaterDAO().insert(theater);
        }
        Log.d(TAG, "Inserted " + theaters.length + " theaters");
    }

    public static void insertSampleShowtimes(AppDB database) {
        String[] dates = {"28/03/2026", "29/03/2026", "30/03/2026", "31/03/2026", "01/04/2026", "02/04/2026"};
        String[] times = {"09:00", "11:30", "14:00", "16:30", "19:00", "21:30"};
        
        int showtimeCount = 0;
        
        for (int movieId = 1; movieId <= 12; movieId++) {
            int theaterCount = (movieId <= 6) ? 6 : 4;
            
            for (int theaterId = 1; theaterId <= theaterCount; theaterId++) {
                int dateCount = (movieId <= 4) ? 6 : 3;
                
                for (int dateIdx = 0; dateIdx < dateCount; dateIdx++) {
                    int timeCount = (movieId <= 2) ? 6 : 3;
                    
                    for (int timeIdx = 0; timeIdx < timeCount; timeIdx++) {
                        int screenNumber = (timeIdx % 3) + 1;
                        int totalSeats = 100;
                        int availableSeats = 100 - (int)(Math.random() * 30);
                        double basePrice = 75000;
                        double timeMultiplier = (timeIdx >= 4) ? 1.3 : 1.0;
                        double price = basePrice * timeMultiplier;
                        
                        Showtime showtime = new Showtime(
                            movieId,
                            theaterId,
                            dates[dateIdx],
                            times[timeIdx],
                            screenNumber,
                            totalSeats,
                            availableSeats,
                            price
                        );
                        database.showtimeDAO().insert(showtime);
                        showtimeCount++;
                    }
                }
            }
        }
        
        Log.d(TAG, "Inserted " + showtimeCount + " showtimes");
    }
}

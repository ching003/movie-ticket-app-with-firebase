package com.example.firebase.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.firebase.entities.Movie;
import com.example.firebase.entities.Showtime;
import com.example.firebase.entities.Theater;
import com.example.firebase.entities.Ticket;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.IntConsumer;

public class FirebaseRepository {
    public interface SingleCallback<T> {
        void onResult(T data);
        void onError(String message);
    }

    private static FirebaseRepository instance;
    private static volatile String databaseUrlOverride;

    private FirebaseAuth auth;
    private DatabaseReference db;
    private final Random random = new Random();

    /** Goi tu {@link com.example.firebase.MovieTicketApp} truoc lan dau {@link #getInstance()}. */
    public static synchronized void setDatabaseUrlOverride(String databaseUrl) {
        databaseUrlOverride = (databaseUrl != null && !databaseUrl.isEmpty()) ? databaseUrl : null;
        instance = null;
    }

    private FirebaseRepository() {
        try {
            auth = FirebaseAuth.getInstance();
            if (databaseUrlOverride != null) {
                db = FirebaseDatabase.getInstance(databaseUrlOverride).getReference();
            } else {
                db = FirebaseDatabase.getInstance().getReference();
            }
        } catch (Exception ignored) {
            auth = null;
            db = null;
        }
    }

    public static synchronized FirebaseRepository getInstance() {
        if (instance == null) {
            instance = new FirebaseRepository();
        }
        return instance;
    }

    public void login(String usernameOrEmail, String password, SingleCallback<FirebaseUser> callback) {
        if (!isReady()) {
            callback.onError("Firebase chua duoc cau hinh. Hay them google-services.json.");
            return;
        }
        String email = usernameOrEmail.contains("@")
                ? usernameOrEmail.toLowerCase(Locale.ROOT)
                : usernameOrEmail.toLowerCase(Locale.ROOT) + "@movieticket.app";

        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(result -> {
            FirebaseUser user = result.getUser();
            saveUserProfile(user, usernameOrEmail);
            callback.onResult(user);
        }).addOnFailureListener(signInError ->
                auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(createResult -> {
                    FirebaseUser user = createResult.getUser();
                    saveUserProfile(user, usernameOrEmail);
                    callback.onResult(user);
                }).addOnFailureListener(createError -> {
                    String c = createError.getMessage() != null ? createError.getMessage() : "";
                    if (c.toLowerCase(Locale.ROOT).contains("already in use")) {
                        callback.onError("Tai khoan da ton tai. Neu quen mat khau, dat lai tren Firebase Console.");
                    } else {
                        callback.onError(c.isEmpty() ? "Dang nhap / dang ky that bai" : c);
                    }
                })
        );
    }

    /**
     * Ghi username + email len RTDB (username_registry/{key}) de luon co san mapping khi seed / debug.
     * Khong luu mat khau.
     */
    public void seedDemoUsernameRegistry(String usernameOrDisplay) {
        if (!isReady() || usernameOrDisplay == null || usernameOrDisplay.trim().isEmpty()) {
            return;
        }
        String raw = usernameOrDisplay.trim();
        String email = raw.contains("@")
                ? raw.toLowerCase(Locale.ROOT)
                : raw.toLowerCase(Locale.ROOT) + "@movieticket.app";
        String loginName = raw.contains("@")
                ? raw.substring(0, raw.indexOf('@'))
                : raw;
        String key = loginName.toLowerCase(Locale.ROOT).replaceAll("[.#$\\[\\]/]", "_");
        if (key.isEmpty()) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("username", loginName);
        map.put("email", email);
        db.child("username_registry").child(key).updateChildren(map);
    }

    /**
     * Tu tao tai khoan demo tren Firebase Auth + ghi users/{uid} tren Realtime DB.
     * Goi khi mo login; neu email da ton tai thi bo qua (lan sau dang nhap binh thuong).
     * Phai trung username/mat khau voi strings.xml (demo / 123456).
     */
    public void ensureDemoAccountOnLaunch(String usernameOrDisplay, String password) {
        seedDemoUsernameRegistry(usernameOrDisplay);
        if (!isReady() || password == null || password.length() < 6) {
            return;
        }
        String email = usernameOrDisplay.contains("@")
                ? usernameOrDisplay.toLowerCase(Locale.ROOT)
                : usernameOrDisplay.toLowerCase(Locale.ROOT) + "@movieticket.app";
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    saveUserProfile(user, usernameOrDisplay);
                })
                .addOnFailureListener(e -> { /* da ton tai: binh thuong */ });
    }

    private void saveUserProfile(FirebaseUser user, String usernameOrEmail) {
        if (user == null) {
            return;
        }
        String username = usernameOrEmail.contains("@")
                ? usernameOrEmail.substring(0, usernameOrEmail.indexOf('@')).trim()
                : usernameOrEmail.trim();
        DatabaseReference ref = db.child("users").child(user.getUid());
        ref.child("uid").setValue(user.getUid());
        ref.child("username").setValue(username);
        ref.child("displayName").setValue(usernameOrEmail);
        ref.child("email").setValue(user.getEmail());
        ref.child("createdAt").setValue(System.currentTimeMillis());
        String key = username.toLowerCase(Locale.ROOT).replaceAll("[.#$\\[\\]/]", "_");
        if (!key.isEmpty()) {
            Map<String, Object> reg = new HashMap<>();
            reg.put("username", username);
            reg.put("email", user.getEmail());
            reg.put("uid", user.getUid());
            db.child("username_registry").child(key).updateChildren(reg);
        }
    }

    public void seedDataIfNeeded() {
        seedDataIfNeeded(null);
    }

    /**
     * Seed phim / rap / suat khi DB trong. Goi sau khi user da dang nhap neu rule RTDB chi cho ghi khi auth != null.
     * {@code onComplete} chay sau khi bo qua (da co du lieu) hoac sau khi ghi xong (hoac loi doc).
     */
    public void seedDataIfNeeded(@Nullable Runnable onComplete) {
        if (!isReady()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        Runnable done = onComplete != null ? onComplete : () -> { };
        db.child("showtimes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot showSnap) {
                db.child("theaters").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot theatersSnap) {
                        db.child("movies").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot moviesSnap) {
                                boolean hasShow = showSnap.exists() && showSnap.getChildrenCount() > 0;
                                boolean hasTheaters = theatersSnap.exists() && theatersSnap.getChildrenCount() > 0;
                                boolean hasMovies = moviesSnap.exists() && moviesSnap.getChildrenCount() > 0;
                                if (hasShow && hasTheaters && hasMovies) {
                                    done.run();
                                    return;
                                }
                                runBulkSeed(done);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                done.run();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        done.run();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                done.run();
            }
        });
    }

    private void runBulkSeed(Runnable done) {
        List<Movie> movies = new ArrayList<>();
        movies.add(new Movie("Dune: Part Two", "Paul Atreides unites with the Fremen.", "Sci-Fi", 166, "https://picsum.photos/seed/dune/400/600", 8.7f, 2024));
        movies.add(new Movie("Inside Out 2", "Riley enters teenage years and meets new emotions.", "Animation", 96, "https://picsum.photos/seed/inside/400/600", 8.1f, 2024));
        movies.add(new Movie("Godzilla x Kong", "Titans must unite against a hidden threat.", "Action", 115, "https://picsum.photos/seed/godzilla/400/600", 7.2f, 2024));
        movies.add(new Movie("Oppenheimer", "The story of J. Robert Oppenheimer and the atomic bomb.", "Biography", 180, "https://picsum.photos/seed/oppen/400/600", 8.4f, 2023));
        movies.add(new Movie("Spider-Man: Across the Spider-Verse", "Miles Morales swings across the multiverse.", "Animation", 140, "https://picsum.photos/seed/spider/400/600", 8.6f, 2023));
        movies.add(new Movie("The Batman", "Batman uncovers corruption in Gotham.", "Action", 176, "https://picsum.photos/seed/batman/400/600", 7.8f, 2022));
        movies.add(new Movie("Poor Things", "Bella Baxter's fantastical evolution.", "Comedy", 141, "https://picsum.photos/seed/poor/400/600", 8.0f, 2023));

        List<Task<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < movies.size(); i++) {
            int id = i + 1;
            movies.get(i).setId(id);
            tasks.add(db.child("movies").child(String.valueOf(id)).setValue(movieToMap(movies.get(i))));
        }

        Theater t1 = new Theater("CGV Vincom", "Quận 1", "45A Lý Tự Trọng, Q1", "19006017", 6);
        Theater t2 = new Theater("Lotte Cinema", "Quận 7", "469 Nguyễn Hữu Thọ, Q7", "1900555567", 8);
        Theater t3 = new Theater("BHD Star", "Quận 3", "330 Lê Văn Sỹ, Q3", "19002099", 5);
        tasks.add(db.child("theaters").child("1").setValue(theaterToMap(t1)));
        tasks.add(db.child("theaters").child("2").setValue(theaterToMap(t2)));
        tasks.add(db.child("theaters").child("3").setValue(theaterToMap(t3)));

        List<Showtime> showtimes = new ArrayList<>();
        showtimes.add(new Showtime(1, 1, "11/04/2026", "19:00", 1, 100, 100, 90000));
        showtimes.add(new Showtime(2, 1, "11/04/2026", "20:30", 2, 80, 80, 75000));
        showtimes.add(new Showtime(3, 2, "11/04/2026", "18:45", 3, 90, 90, 85000));
        showtimes.add(new Showtime(1, 2, "11/04/2026", "21:15", 4, 90, 90, 95000));
        showtimes.add(new Showtime(4, 1, "12/04/2026", "14:00", 1, 100, 100, 95000));
        showtimes.add(new Showtime(5, 2, "12/04/2026", "16:30", 2, 80, 80, 80000));
        showtimes.add(new Showtime(6, 3, "11/04/2026", "22:00", 1, 120, 120, 88000));
        showtimes.add(new Showtime(7, 1, "13/04/2026", "10:00", 3, 100, 100, 70000));
        for (int i = 0; i < showtimes.size(); i++) {
            int id = i + 1;
            showtimes.get(i).setId(id);
            tasks.add(db.child("showtimes").child(String.valueOf(id)).setValue(showtimeToMap(showtimes.get(i))));
        }

        Tasks.whenAll(tasks).addOnCompleteListener(task -> done.run());
    }

    private static Map<String, Object> movieToMap(Movie m) {
        Map<String, Object> map = new HashMap<>();
        map.put("title", m.getTitle());
        map.put("description", m.getDescription());
        map.put("genre", m.getGenre());
        map.put("duration", m.getDuration());
        map.put("posterUrl", m.getPosterUrl());
        map.put("rating", (double) m.getRating());
        map.put("releaseYear", m.getReleaseYear());
        return map;
    }

    private static Map<String, Object> theaterToMap(Theater t) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", t.getName());
        map.put("location", t.getLocation());
        map.put("address", t.getAddress());
        map.put("phone", t.getPhone());
        map.put("totalScreens", t.getTotalScreens());
        return map;
    }

    private static Map<String, Object> showtimeToMap(Showtime s) {
        Map<String, Object> map = new HashMap<>();
        map.put("movieId", s.getMovieId());
        map.put("theaterId", s.getTheaterId());
        map.put("showDate", s.getShowDate());
        map.put("showTime", s.getShowTime());
        map.put("screenNumber", s.getScreenNumber());
        map.put("totalSeats", s.getTotalSeats());
        map.put("availableSeats", s.getAvailableSeats());
        map.put("price", s.getPrice());
        return map;
    }

    /**
     * Neu user chua co ve nao, tao 3 ve mau (suat 1–3) va giam ghe trong.
     * ID ve trong khoang 3_000_000+ (tranh 100000–999999 cua createTickets), khong trung key da co.
     */
    public void ensureSampleTicketsForUser(String userUid) {
        if (!isReady() || userUid == null || userUid.isEmpty()) {
            return;
        }
        db.child("tickets").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ticketsSnap) {
                int userTicketCount = 0;
                for (DataSnapshot item : ticketsSnap.getChildren()) {
                    Ticket t = item.getValue(Ticket.class);
                    if (t != null && userUid.equals(t.getUserUid())) {
                        userTicketCount++;
                    }
                }
                if (userTicketCount > 0) {
                    return;
                }
                db.child("showtimes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot showSnap) {
                        if (!showSnap.hasChild("1") || !showSnap.hasChild("2") || !showSnap.hasChild("3")) {
                            return;
                        }
                        long bookingDate = System.currentTimeMillis() - 48L * 3600_000L;
                        Object[][] defs = {
                                {1, 90000d, 1, "A10", "Dune: Part Two", "CGV Vincom", "11/04/2026", "19:00"},
                                {2, 75000d, 2, "B5", "Inside Out 2", "CGV Vincom", "11/04/2026", "20:30"},
                                {3, 85000d, 3, "C12", "Godzilla x Kong", "Lotte Cinema", "11/04/2026", "18:45"},
                        };
                        Random idRand = new Random(userUid.hashCode());
                        int[] ticketIds = new int[3];
                        for (int i = 0; i < 3; i++) {
                            int candidate = 3_000_000 + i;
                            for (int guard = 0; guard < 500; guard++) {
                                candidate = 3_000_000 + idRand.nextInt(1_000_000);
                                if (ticketsSnap.hasChild(String.valueOf(candidate))) {
                                    continue;
                                }
                                boolean dupAmongNew = false;
                                for (int j = 0; j < i; j++) {
                                    if (candidate == ticketIds[j]) {
                                        dupAmongNew = true;
                                        break;
                                    }
                                }
                                if (!dupAmongNew) {
                                    break;
                                }
                            }
                            ticketIds[i] = candidate;
                        }
                        for (int i = 0; i < defs.length; i++) {
                            Object[] row = defs[i];
                            int showtimeId = (Integer) row[0];
                            double price = (Double) row[1];
                            int screen = (Integer) row[2];
                            Ticket ticket = new Ticket(0, showtimeId, (String) row[3], bookingDate, price, "BOOKED");
                            ticket.setId(ticketIds[i]);
                            ticket.setUserUid(userUid);
                            ticket.setMovieTitle((String) row[4]);
                            ticket.setTheaterName((String) row[5]);
                            ticket.setShowDate((String) row[6]);
                            ticket.setShowTime((String) row[7]);
                            ticket.setScreenNumber(screen);
                            db.child("tickets").child(String.valueOf(ticketIds[i])).setValue(ticket);
                        }
                        for (int sid = 1; sid <= 3; sid++) {
                            Showtime st = showSnap.child(String.valueOf(sid)).getValue(Showtime.class);
                            if (st != null) {
                                int next = Math.max(0, st.getAvailableSeats() - 1);
                                db.child("showtimes").child(String.valueOf(sid)).child("availableSeats").setValue(next);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public void getTheaters(SingleCallback<List<Theater>> callback) {
        if (!isReady()) {
            callback.onError("Firebase chua san sang");
            return;
        }
        db.child("theaters").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Theater> list = new ArrayList<>();
                for (DataSnapshot item : snapshot.getChildren()) {
                    Theater t = parseTheater(item);
                    if (t != null && t.getName() != null && !t.getName().isEmpty()) {
                        list.add(t);
                    }
                }
                callback.onResult(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    /**
     * Lang nghe lien tuc nhanh theaters — UI cap nhat khi seed ghi xong hoac du lieu doi.
     * Goi {@link #removeTheatersListener(ValueEventListener)} trong {@code Activity.onStop}.
     */
    public ValueEventListener addTheatersValueEventListener(SingleCallback<List<Theater>> callback) {
        if (!isReady()) {
            callback.onError("Firebase chua san sang");
            return null;
        }
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Theater> list = new ArrayList<>();
                for (DataSnapshot item : snapshot.getChildren()) {
                    Theater t = parseTheater(item);
                    if (t != null && t.getName() != null && !t.getName().isEmpty()) {
                        list.add(t);
                    }
                }
                callback.onResult(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        };
        db.child("theaters").addValueEventListener(listener);
        return listener;
    }

    public void removeTheatersListener(@Nullable ValueEventListener listener) {
        if (!isReady() || listener == null) {
            return;
        }
        db.child("theaters").removeEventListener(listener);
    }

    public void getMovieById(int movieId, SingleCallback<Movie> callback) {
        if (!isReady()) {
            callback.onError("Firebase chua san sang");
            return;
        }
        db.child("movies").child(String.valueOf(movieId)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Movie movie = parseMovie(snapshot);
                if (movie == null || movie.getTitle() == null || movie.getTitle().isEmpty()) {
                    callback.onError("Không tìm thấy phim");
                    return;
                }
                movie.setId(movieId);
                callback.onResult(movie);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void getShowtimeById(int showtimeId, SingleCallback<Showtime> callback) {
        if (!isReady()) {
            callback.onError("Firebase chua san sang");
            return;
        }
        db.child("showtimes").child(String.valueOf(showtimeId)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Showtime showtime = parseShowtime(snapshot);
                if (showtime == null) {
                    callback.onError("Không tìm thấy suất chiếu");
                    return;
                }
                showtime.setId(showtimeId);
                callback.onResult(showtime);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void getMoviesByTheater(int theaterId, SingleCallback<List<Movie>> callback) {
        if (!isReady()) {
            callback.onError("Firebase chua san sang");
            return;
        }
        getShowtimesByTheater(theaterId, new SingleCallback<List<Showtime>>() {
            @Override
            public void onResult(List<Showtime> showtimes) {
                Set<Integer> ids = new HashSet<>();
                for (Showtime showtime : showtimes) {
                    ids.add(showtime.getMovieId());
                }
                db.child("movies").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Movie> movies = new ArrayList<>();
                        for (DataSnapshot item : snapshot.getChildren()) {
                            Movie movie = parseMovie(item);
                            if (movie == null) {
                                continue;
                            }
                            int id = Integer.parseInt(item.getKey());
                            if (ids.contains(id)) {
                                movie.setId(id);
                                movies.add(movie);
                            }
                        }
                        callback.onResult(movies);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void getShowtimesByTheater(int theaterId, SingleCallback<List<Showtime>> callback) {
        if (!isReady()) {
            callback.onError("Firebase chua san sang");
            return;
        }
        db.child("showtimes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Showtime> list = new ArrayList<>();
                for (DataSnapshot item : snapshot.getChildren()) {
                    Showtime showtime = parseShowtime(item);
                    if (showtime == null) {
                        continue;
                    }
                    if (showtime.getTheaterId() == theaterId) {
                        list.add(showtime);
                    }
                }
                callback.onResult(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void getShowtimesByMovieAndTheater(int movieId, int theaterId, SingleCallback<List<Showtime>> callback) {
        getShowtimesByTheater(theaterId, new SingleCallback<List<Showtime>>() {
            @Override
            public void onResult(List<Showtime> data) {
                List<Showtime> filtered = new ArrayList<>();
                for (Showtime showtime : data) {
                    if (showtime.getMovieId() == movieId) {
                        filtered.add(showtime);
                    }
                }
                callback.onResult(filtered);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void getBookedSeats(int showtimeId, SingleCallback<List<String>> callback) {
        if (!isReady()) {
            callback.onError("Firebase chua san sang");
            return;
        }
        db.child("tickets").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> seats = new ArrayList<>();
                for (DataSnapshot item : snapshot.getChildren()) {
                    Ticket ticket = item.getValue(Ticket.class);
                    if (ticket == null) {
                        continue;
                    }
                    if ("BOOKED".equals(ticket.getStatus()) && ticket.getShowtimeId() == showtimeId) {
                        seats.add(ticket.getSeatNumber());
                    }
                }
                callback.onResult(seats);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void createTickets(String userUid, Showtime showtime, String movieTitle, String theaterName, List<String> selectedSeats, SingleCallback<List<Ticket>> callback) {
        if (!isReady()) {
            callback.onError("Firebase chua san sang");
            return;
        }
        long bookingDate = System.currentTimeMillis();
        List<Ticket> created = new ArrayList<>();
        for (String seat : selectedSeats) {
            Ticket ticket = new Ticket(0, showtime.getId(), seat, bookingDate, showtime.getPrice(), "BOOKED");
            ticket.setId(100000 + random.nextInt(900000));
            ticket.setUserUid(userUid);
            ticket.setMovieTitle(movieTitle);
            ticket.setTheaterName(theaterName);
            ticket.setShowDate(showtime.getShowDate());
            ticket.setShowTime(showtime.getShowTime());
            ticket.setScreenNumber(showtime.getScreenNumber());
            created.add(ticket);
            db.child("tickets").child(String.valueOf(ticket.getId())).setValue(ticket);
        }

        int newAvailableSeats = Math.max(0, showtime.getAvailableSeats() - selectedSeats.size());
        db.child("showtimes").child(String.valueOf(showtime.getId())).child("availableSeats")
                .setValue(newAvailableSeats)
                .addOnSuccessListener(unused -> callback.onResult(created))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getTicketsByUser(String userUid, SingleCallback<List<Ticket>> callback) {
        if (!isReady()) {
            callback.onError("Firebase chua san sang");
            return;
        }
        db.child("tickets").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Ticket> tickets = new ArrayList<>();
                for (DataSnapshot item : snapshot.getChildren()) {
                    Ticket ticket = item.getValue(Ticket.class);
                    if (ticket == null) {
                        continue;
                    }
                    if (userUid.equals(ticket.getUserUid())) {
                        tickets.add(ticket);
                    }
                }
                callback.onResult(tickets);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    private Theater parseTheater(DataSnapshot item) {
        Theater t = item.getValue(Theater.class);
        if (t != null && t.getName() != null && !t.getName().isEmpty()) {
            setIdFromKey(t::setId, item.getKey());
            return t;
        }
        String name = readStringChild(item, "name");
        if (name == null || name.isEmpty()) {
            return null;
        }
        Theater out = new Theater();
        setIdFromKey(out::setId, item.getKey());
        out.setName(name);
        out.setLocation(nvl(readStringChild(item, "location")));
        out.setAddress(nvl(readStringChild(item, "address")));
        out.setPhone(nvl(readStringChild(item, "phone")));
        int screens = (int) readLongChild(item, 0, "totalScreens", "total_screens");
        out.setTotalScreens(screens);
        return out;
    }

    private Movie parseMovie(DataSnapshot item) {
        Movie m = item.getValue(Movie.class);
        if (m != null && m.getTitle() != null && !m.getTitle().isEmpty()) {
            setIdFromKey(m::setId, item.getKey());
            return m;
        }
        String title = readStringChild(item, "title");
        if (title == null || title.isEmpty()) {
            return null;
        }
        Movie out = new Movie();
        setIdFromKey(out::setId, item.getKey());
        out.setTitle(title);
        out.setDescription(nvl(readStringChild(item, "description")));
        out.setGenre(nvl(readStringChild(item, "genre")));
        out.setDuration((int) readLongChild(item, 0, "duration"));
        out.setPosterUrl(nvl(readStringChild(item, "posterUrl", "poster_url")));
        out.setRating((float) readDoubleChild(item, 0, "rating"));
        out.setReleaseYear((int) readLongChild(item, 0, "releaseYear", "release_year"));
        return out;
    }

    private Showtime parseShowtime(DataSnapshot item) {
        Showtime s = item.getValue(Showtime.class);
        if (s != null && (s.getTheaterId() != 0 || s.getMovieId() != 0)) {
            setIdFromKey(s::setId, item.getKey());
            return s;
        }
        Showtime out = new Showtime();
        setIdFromKey(out::setId, item.getKey());
        out.setMovieId((int) readLongChild(item, 0, "movieId", "movie_id"));
        out.setTheaterId((int) readLongChild(item, 0, "theaterId", "theater_id"));
        out.setShowDate(nvl(readStringChild(item, "showDate", "show_date")));
        out.setShowTime(nvl(readStringChild(item, "showTime", "show_time")));
        out.setScreenNumber((int) readLongChild(item, 0, "screenNumber", "screen_number"));
        out.setTotalSeats((int) readLongChild(item, 0, "totalSeats", "total_seats"));
        out.setAvailableSeats((int) readLongChild(item, 0, "availableSeats", "available_seats"));
        out.setPrice(readDoubleChild(item, 0, "price"));
        if (out.getTheaterId() == 0 && out.getMovieId() == 0) {
            return null;
        }
        return out;
    }

    private static void setIdFromKey(IntConsumer setter, String key) {
        try {
            setter.accept(Integer.parseInt(key));
        } catch (Exception ignored) {
            setter.accept(0);
        }
    }

    private static String nvl(String s) {
        return s != null ? s : "";
    }

    private static String readStringChild(DataSnapshot node, String... keys) {
        for (String k : keys) {
            if (node.hasChild(k)) {
                Object v = node.child(k).getValue();
                if (v != null) {
                    return String.valueOf(v);
                }
            }
        }
        return null;
    }

    private static long readLongChild(DataSnapshot node, long def, String... keys) {
        for (String k : keys) {
            if (!node.hasChild(k)) {
                continue;
            }
            Object v = node.child(k).getValue();
            if (v instanceof Long) {
                return (Long) v;
            }
            if (v instanceof Integer) {
                return (Integer) v;
            }
            if (v instanceof Double) {
                return ((Double) v).longValue();
            }
        }
        return def;
    }

    private static double readDoubleChild(DataSnapshot node, double def, String... keys) {
        for (String k : keys) {
            if (!node.hasChild(k)) {
                continue;
            }
            Object v = node.child(k).getValue();
            if (v instanceof Double) {
                return (Double) v;
            }
            if (v instanceof Long) {
                return ((Long) v).doubleValue();
            }
            if (v instanceof Integer) {
                return ((Integer) v).doubleValue();
            }
        }
        return def;
    }

    private boolean isReady() {
        if (auth != null && db != null) {
            return true;
        }
        try {
            auth = FirebaseAuth.getInstance();
            if (databaseUrlOverride != null) {
                db = FirebaseDatabase.getInstance(databaseUrlOverride).getReference();
            } else {
                db = FirebaseDatabase.getInstance().getReference();
            }
        } catch (Exception ignored) {
            auth = null;
            db = null;
        }
        return auth != null && db != null;
    }
}

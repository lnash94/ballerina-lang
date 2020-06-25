type Order record {
        int id;
        int petId;
        int quantity;
        string shipDate;
        string status;
        boolean complete;
};
type Category record {
    int id;
    string name;
};
type User record {
    int id;
    string username;
    string firstName;
    string lastName;
    string email2;
    string password;
    string phone;
    int userStatus;
};
type Tag record {
    int id;
    string name;
};
type Pet record {
    int id;
    Category category;
    string name;
    string [] photoUrls;
    Tag [] tags;
    string status;
};
type ApiResponse record {
    int code;
    string 'type;
    string message;
};
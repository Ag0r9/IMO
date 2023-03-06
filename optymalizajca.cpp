#include <iostream>
#include <fstream>
#include <string>
using namespace std;

struct Node{
    int x;
    int y;
};

void load_data(Node data[100], string filename){
    fstream file;
    file.open(filename, ios::in);
    string x;
    for(int i=0; i<6;i++ )
        getline(file, x);

    for(int i =0 ; i<100; i++)
    {
        file >> x;
        file >> x; data[i].x = stoi(x);
        file >> x; data[i].y = stoi(x);
    }
    file.close();
}

int main(){
    Node nodes[100];
    
    load_data(nodes, "kroA100.tsp");
    for(int p = 0 ; p<100;p++)
    {
        cout << p+1 << ". "<< nodes[p].x << " " << nodes[p].y<<endl;
    }
}
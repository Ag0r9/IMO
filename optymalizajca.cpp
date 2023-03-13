#include <iostream>
#include <fstream>
#include <string>
#include <cmath>
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

double **calculate_distance (Node nodes[100]){
    double** dist = new double*[100];
    for (int i=0; i<100; i++){
        dist[i] = new double[100];
        for(int j=0;j<100;j++){
            double dx = nodes[i].x - nodes[j].x;
            double dy = nodes[i].y - nodes[j].y;
            dist[i][j] = sqrt(dx*dx + dy*dy);
        }
    }
    return dist;
}

int main(){
    Node nodes[100];
    load_data(nodes, "data/kroA100.tsp");
    double** distances = calculate_distance(nodes);
    for(int i=0; i<100;i++)
        {
        for(int j=0; j<100;j++){
            cout <<distances[i][j]<< " ";
        }
        cout <<endl;
        delete distances[i];
        }
    delete distances;
}
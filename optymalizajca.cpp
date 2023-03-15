#include <iostream>
#include <fstream>
#include <string>
#include <cmath>
#include <cstdio>
#include <vector>
#include <algorithm>
using namespace std;

struct Node{
    int x;
    int y;
};

void load_data(Node data[100], string filename){
    fstream file;
    file.open(filename, ios::in);
    string x;
    for(int i = 0; i < 6; i++ )
        getline(file, x);

    for(int i = 0 ; i < 100; i++)
    {
        file >> x;
        file >> x; data[i].x = stoi(x);
        file >> x; data[i].y = stoi(x);
    }
    file.close();
}

double **calculate_distance (Node nodes[100]){
    double** dist = new double*[100];
    for (int i = 0; i < 100; i++){
        dist[i] = new double[100];
        for(int j = 0; j < 100; j++){
            double dx = nodes[i].x - nodes[j].x;
            double dy = nodes[i].y - nodes[j].y;
            dist[i][j] = sqrt(pow(dx,2) + pow(dy,2));
        }
    }
    return dist;
}

vector<int> nearest_neighbour(Node nodes[100], double **dist){
    vector<int> not_used;
    for (int i = 1; i < 100; i++) 
        not_used.push_back(i);
    
    vector <int> solution;
    solution.push_back(0);
    int last = solution[0];

    while(!not_used.empty()){
        int min_dist=INT32_MAX;
        int min_id = -1;
        for(auto i = not_used.begin(); i!=not_used.end();i++){
            if(dist[last][*i] < min_dist){
                min_id = *i;
                min_dist = dist[last][*i];
            }
        }
        solution.push_back(min_id);
        last=min_id;
        auto q = find(not_used.begin(),not_used.end(),min_id);
        not_used.erase(q);
    }
    return solution;
}

int main(){
    Node nodes[100];
    load_data(nodes, "data/kroA100.tsp");
    double** distances = calculate_distance(nodes);
    
    vector<int> x = nearest_neighbour(nodes, distances);    
    for(int y=0;y<100;y++)
        cout<< x[y] <<" ";

    //for(int i = 0; i < 100; i++){
    //    for(int j = 0; j < 100; j++){
    //        delete distances[i];
    //    }
    //}    
    //delete distances;
    return 0;
}

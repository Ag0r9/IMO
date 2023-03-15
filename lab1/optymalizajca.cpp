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

vector<int> nearest_neighbour(Node nodes[100], double **dist, int start_id, int end_id){
    vector<int> not_used;
    for (int i = 0; i < 100; i++){
        if(i != start_id && i != end_id ) 
            not_used.push_back(i);
    }
    vector <int> solution;
    solution.push_back(start_id);
    solution.push_back(end_id);
    while(!not_used.empty()){
        int min_dist = INT32_MAX;
        int min_id = -1;
        int after_whom_in_solution = -1;
        for(auto i = not_used.begin(); i!=not_used.end();i++){
            for(int j = 0; j+1 < solution.size(); j++)
                {
                    if(dist[solution[j]][*i] + dist[*i][solution[j+1]]< min_dist){
                        min_id = *i;
                        min_dist = dist[solution[j]][*i] + dist[*i][solution[j+1]];
                        after_whom_in_solution = solution[j];
                }
            }
        }
        auto q = find(solution.begin(), solution.end(), after_whom_in_solution);
        solution.insert(q,min_id);
        auto r = find(not_used.begin(), not_used.end(), min_id);
        not_used.erase(r);
    }
    return solution;
}

int main(){
    srand( time( NULL ) );
    Node nodes[100];
    load_data(nodes, "data/kroA100.tsp");
    double** distances = calculate_distance(nodes);
    
    int end_id, start_id = rand() % 100;
    do{
    end_id = rand() % 100;
    }while(end_id == start_id);
    vector<int> x = nearest_neighbour(nodes, distances, start_id, end_id);    

    for(int i = 0; i<100;i++)
        cout<< x[i] <<" ";
    //for(int i = 0; i < 100; i++){
    //    for(int j = 0; j < 100; j++){
    //        delete distances[i];
    //    }
    //}    
    //delete distances;
    return 0;
}
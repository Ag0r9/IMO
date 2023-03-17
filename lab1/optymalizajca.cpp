#include <iostream>
#include <fstream>
#include <string>
#include <cmath>
#include <cstdio>
#include <vector>
#include <algorithm>
#include <float.h>
using namespace std;

struct Node
{
    int x;
    int y;
};

void load_data(Node *data, string filename)
{
    fstream file;
    file.open(filename, ios::in);
    string x;
    for (int i = 0; i < 6; i++)
        getline(file, x);

    for (int i = 0; i < 100; i++)
    {
        file >> x;
        file >> x;
        data[i].x = stoi(x);
        file >> x;
        data[i].y = stoi(x);
    }
    file.close();
}

double **calculate_distance(Node *nodes)
{
    double **dist = new double *[100];
    for (int i = 0; i < 100; i++)
    {
        dist[i] = new double[100];
        for (int j = 0; j < 100; j++)
        {
            double dx = nodes[i].x - nodes[j].x;
            double dy = nodes[i].y - nodes[j].y;
            dist[i][j] = sqrt(pow(dx, 2) + pow(dy, 2));
        }
    }
    return dist;
}

void cycle_creation(double **dist, vector<int> &not_used, vector<int> &solution)
{
    double min_dist = DBL_MAX;
    int min_id = -1;
    int after_whom_in_solution = -1;
    for (auto i = not_used.begin(); i != not_used.end(); i++)
    {
        for (int j = 0; j + 1 < solution.size(); j++)
        {
            double w = dist[solution[j]][*i] + dist[*i][solution[j + 1]] - dist[solution[j]][solution[j + 1]];
            if (w < min_dist)
            {
                min_id = *i;
                min_dist = w;
                after_whom_in_solution = solution[j];
            }
        }
    }
    solution.insert(++(find(solution.begin(), solution.end(), after_whom_in_solution)), min_id);
    not_used.erase(find(not_used.begin(), not_used.end(), min_id));
}

void find_nearest(double **dist, vector<int> &not_used, vector<int> &solution)
{
    double min_dist = DBL_MAX;
    int min_id = -1;
    int after_whom_in_solution = -1;
    for (auto i = not_used.begin(); i != not_used.end(); i++)
    {
        for (int j = 0; j + 1 < solution.size(); j++)
        {
            double w = dist[solution[j]][*i] + dist[*i][solution[j + 1]] - dist[solution[j]][solution[j + 1]];
            if (w < min_dist)
            {
                min_id = *i;
                min_dist = w;
                after_whom_in_solution = solution[j];
            }
        }
        int a = *(--solution.end());
        int b = dist[a][*i];
        if (b < min_dist)
        {
            min_id = *i;
            min_dist = b;
            after_whom_in_solution = a;
        }
    }
    solution.insert(++(find(solution.begin(), solution.end(), after_whom_in_solution)), min_id);
    not_used.erase(find(not_used.begin(), not_used.end(), min_id));
}

vector<vector<int>> nearest_neighbour(double **dist, int first_start, int second_start)
{
    vector<int> not_used;
    for (int i = 0; i < 100; i++)
    {
        if (i != first_start && i != second_start)
            not_used.push_back(i);
    }
    vector<int> solution1, solution2;
    solution1.push_back(first_start);
    while (solution1.size() < 50)
    {
        find_nearest(dist, not_used, solution1);
    }
    solution1.push_back(first_start);
    solution2.push_back(second_start);
    while (solution2.size() < 50)
    {
        find_nearest(dist, not_used, solution2);
    }
    solution2.push_back(second_start);

    vector<vector<int>> x;
    x.push_back(solution1);
    x.push_back(solution2);
    return x;
}

vector<vector<int>> greedy_cycle(double **dist, int first_start, int second_start)
{
    vector<int> not_used;
    for (int i = 0; i < 100; i++)
    {
        if (i != first_start && i != second_start)
            not_used.push_back(i);
    }
    vector<int> solution1, solution2;
    solution1.push_back(first_start);
    solution1.push_back(first_start);
    while (solution1.size() < 51)
        cycle_creation(dist, not_used, solution1);
    
    solution2.push_back(second_start);
    solution2.push_back(second_start);
    while (solution2.size() < 51)
        cycle_creation(dist, not_used, solution2);

    vector<vector<int>> x;
    x.push_back(solution1);
    x.push_back(solution2);
    return x;
}

int main()
{
    srand(time(NULL));
    Node* nodes = new Node[100];
    load_data(nodes, "data/kroA100.tsp");
    double **distances = calculate_distance(nodes);

    int second_id, first_id = rand() % 100;
    double max_dist = 0.0;
    for (int i = 0; i < 100; i++)
    {
        if (distances[i][first_id] > max_dist)
        {
            second_id = i;
            max_dist = distances[i][first_id];
        }
    }
    vector<vector<int>> x = greedy_cycle(distances, first_id, second_id);

    double d = 0.0;
    for (int i = 0; i < 50; i++)
        d += distances[x[0][i]][x[0][i + 1]];

    for (int i = 0; i < 50; i++)
        d += distances[x[1][i]][x[1][i + 1]];

    cout << "Greedy cycle: " << d << endl;

    vector<vector<int>> y = nearest_neighbour(distances, first_id, second_id);
    int d2 = 0.0;
    for (int i = 0; i < 50; i++)
    {
        d2 += distances[y[0][i]][y[0][i + 1]];
    }

    for (int i = 0; i < 50; i++)
        d2 += distances[y[1][i]][y[1][i + 1]];

    cout << "Nearest neighbour: " << d2 << endl;

    delete nodes;
    delete[] distances;
    
    return 0;
}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment'; // ou ton chemin vers environment

@Injectable({ providedIn: 'root' })
export class ApiService {
  // private baseUrl = environment.apiUrl; // définie dans tes fichiers d'env
  //
  // constructor(private http: HttpClient) {}
  //
  // envoyerPetition(data: any) {
  //   return this.http.post(`${this.baseUrl}/petition`, data);
  // }
}

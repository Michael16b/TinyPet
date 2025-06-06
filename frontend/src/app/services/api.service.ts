import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  private baseUrl: string = window.location.protocol + "//" + window.location.host;

  getBaseUrl(): string {
    return this.baseUrl;
  }

  setBaseUrl(url: string): void {
    this.baseUrl = url;
  }

  async sendPetition(petitionData: any): Promise<any> {
    try {
      const response = await fetch(`${this.baseUrl}/_ah/api/petitionApi/v1/create`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(petitionData),
      });
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      return await response.json();
    } catch (error) {
      console.error('There was a problem with the fetch operation:', error);
      throw error;
    }
  }
}

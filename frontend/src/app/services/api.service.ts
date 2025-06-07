import { Injectable } from '@angular/core';
import {UserService} from './user.service';
import {MatSnackBar} from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  private baseUrl: string = window.location.protocol + "//" + window.location.host;
  private baseRealUrlBackend: string = "https://tinypet-atalla-besily-jan.ew.r.appspot.com";

  constructor(
    private snackBar: MatSnackBar
  ) {}

  getBaseUrl(): string {
    return this.baseUrl;
  }

  setBaseUrl(url: string): void {
    this.baseUrl = url;
  }

  async sendPetition(petitionData: any): Promise<any> {
    try {
      const response = await fetch(`${this.baseRealUrlBackend}/_ah/api/petitionApi/v1/create`, {
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

  async getPetitionList(
    accessToken: string,
    limit: number,
    cursor?: string,
    sortBy?: string,
    sortOrder?: string,
    tag?: string
  ): Promise<any> {
    let url = `${this.baseRealUrlBackend}/_ah/api/petitionApi/v1/list?access_token=${encodeURIComponent(accessToken)}&limit=${encodeURIComponent(limit)}`;
    if (sortBy) {
      url += `&sortBy=${encodeURIComponent(sortBy)}`;
    }
    if (sortOrder) {
      url += `&sortOrder=${encodeURIComponent(sortOrder)}`;
    }
    if (tag) {
      url += `&tag=${encodeURIComponent(tag)}`;
    }
    if (cursor) {
      url += `&cursor=${encodeURIComponent(cursor)}`;
    }

    console.log(url);
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${accessToken}`
      }
    });
    if (!response.ok) {
      throw new Error('Failed to fetch petition list');
    }
    return await response.json();
  }

  async signPetition(accessToken: string, petitionId: string): Promise<any> {
    const url = `${this.baseRealUrlBackend}/_ah/api/petitionApi/v1/sign?petitionId=${encodeURIComponent(petitionId)}&access_token=${encodeURIComponent(accessToken)}`;
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      }
    });
    console.log("Signing petition with URL: ", url);

    let data: any = null;
    try {
      data = await response.json();
    } catch {
    }
    if (!response.ok) {
      const message = data?.error?.message || data?.message || 'Failed to sign petition';
      const error = new Error(message);
      (error as any).httpStatus = response.status;
      this.snackBar.open(message, '', { duration: 2000 });
      throw error;
    }
    this.snackBar.open('Merci pour votre signature !', '', { duration: 2000 });
    return data;
  }

  async getSigners(accessToken: string, petitionId: string): Promise<any[]> {
    const url = `${this.baseRealUrlBackend}/_ah/api/petitionApi/v1/petition/${petitionId}/signers/?access_token=${encodeURIComponent(accessToken)}`;
    const response = await fetch(url, { headers: { 'Content-Type': 'application/json' } });
    const data = await response.json();
    return data.signers;
  }
}

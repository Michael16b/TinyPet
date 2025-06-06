import { Component } from '@angular/core';
import {RouterLink} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {CreatePetitionDialogComponent} from '../dialogs/create-petition-dialog/create-petition-dialog.component';
import {ApiService} from '../services/api.service';
import {CookiesService} from '../services/cookies.service';

@Component({
  selector: 'app-home',
  imports: [

  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {
  constructor(public dialog: MatDialog, private apiService : ApiService) {}


  openCreatePetitionDialog(): void {
    const dialogRef = this.dialog.open(CreatePetitionDialogComponent, {
      width: '800px',
      height: '500px',
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.apiService.sendPetition(result);
      }
    });
  }
}

import { Component } from '@angular/core';
import {RouterLink} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {CreatePetitionDialogComponent} from '../dialogs/create-petition-dialog/create-petition-dialog.component';
import {ApiService} from '../services/api.service';

@Component({
  selector: 'app-home',
  imports: [

  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {
  constructor(public dialog: MatDialog, private apiService: ApiService) {}


  openCreatePetitionDialog(): void {
    const dialogRef = this.dialog.open(CreatePetitionDialogComponent, {
      width: '800px',
      height: '500px',
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // this.apiService.envoyerPetition(result).subscribe({
        //   next: (response) => {
        //     console.log('Pétition créée côté backend:', response);
        //   },
        //   error: (err) => {
        //     console.error('Erreur lors de l\'envoi:', err);
        //   }
        // });
      }
    });
  }
}

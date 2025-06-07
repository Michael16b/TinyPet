import { Component } from '@angular/core';
import {Router} from '@angular/router';
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
  constructor(public dialog: MatDialog, private apiService : ApiService, private router : Router) {}


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

  openPetitionView() {
    this.router.navigate(['/petition']);
  }
}

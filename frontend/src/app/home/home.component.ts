import { Component, OnInit, OnDestroy } from '@angular/core';
import {Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {CreatePetitionDialogComponent} from '../dialogs/create-petition-dialog/create-petition-dialog.component';
import {ApiService} from '../services/api.service';
import { UserService } from '../services/user.service';
import { LoginDialogComponent } from '../dialogs/login-dialog/login-dialog.component';
import { Subscription } from 'rxjs';


@Component({
  selector: 'app-home',
  imports: [

  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {
  loggedIn = false;
  private subscription?: Subscription;
  constructor(public dialog: MatDialog, private apiService : ApiService, private router : Router, public userService : UserService) {}

  openCreatePetitionDialog(): void {
    if (!this.loggedIn) {
      const dialogRef = this.dialog.open(LoginDialogComponent, {
        width: '400px',
        data: {
          message: 'Veuillez vous connecter pour créer une pétition.'
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.openCreatePetitionDialog();
        }
      });

      return;
    }

    const petitionDialogRef = this.dialog.open(CreatePetitionDialogComponent, {
      width: '800px',
      height: '500px',
    });

    petitionDialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.apiService.sendPetition(result);
      }
    });
  }


  openPetitionView(): void {
    if (this.loggedIn) {
      this.router.navigate(['/petition']);
    } else {
      this.dialog.open(LoginDialogComponent, {
        width: '400px',
        data: {
          message: 'Veuillez vous connecter pour accéder à cette section.',
        }
      });
    }
  }

}

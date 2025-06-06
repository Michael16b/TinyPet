import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { LoginDialogComponent } from '../../dialogs/login-dialog/login-dialog.component';
import { Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';


@Component({
  selector: 'app-must-login-dialog',
  templateUrl: './must-login-dialog.component.html',
  styleUrls: ['./must-login-dialog.component.css'],
  imports: [
    MatDialogModule,
    MatButtonModule
  ],
  standalone: true
})
export class MustLoginDialogComponent {

  constructor(
    public dialogRef: MatDialogRef<MustLoginDialogComponent>,
    private dialog: MatDialog,
    @Inject(MAT_DIALOG_DATA) public data: { title: string, description: string, tags: string[] }
  ) {}

  close(): void {
    this.dialogRef.close();
  }

  goToLogin(): void {
    this.dialogRef.close();
    const loginRef = this.dialog.open(LoginDialogComponent, { width: '500px' });

    loginRef.afterClosed().subscribe((result) => {
      if (result === true) {
        this.dialogRef.close({
          loggedIn: true,
          title: this.data.title,
          description: this.data.description,
          tags: this.data.tags
        });
      }
    });
  }
}
